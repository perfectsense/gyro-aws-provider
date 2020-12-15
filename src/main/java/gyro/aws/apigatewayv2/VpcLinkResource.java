/*
 * Copyright 2020, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.apigatewayv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.CreateVpcLinkResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetVpcLinksRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetVpcLinksResponse;
import software.amazon.awssdk.services.apigatewayv2.model.VpcLink;
import software.amazon.awssdk.services.apigatewayv2.model.VpcLinkStatus;

/**
 * Create a vpc link.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-vpc-link example-vpc-link
 *         name: "example-vpc-link"
 *         security-groups: [ $(aws::security-group security-group-example) ]
 *
 *         subnets: [
 *             $(aws::subnet subnet-example-2a),
 *             $(aws::subnet subnet-example-2b)
 *         ]
 *
 *         tags: {
 *             "example-key" : "example-value"
 *         }
 *     end
 */
@Type("api-gateway-vpc-link")
public class VpcLinkResource extends AwsResource implements Copyable<VpcLink> {

    private String name;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;
    private Map<String, String> tags;

    // Output
    private String id;
    private String arn;

    /**
     * The name of the VPC link.
     */
    @Updatable
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of security groups for the VPC link.
     */
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The list of subnets for the VPC link.
     */
    @Required
    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The list of tags for the VPC link.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ID of the VPC Link.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ARN of the Vpc Link.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(VpcLink model) {
        setId(model.vpcLinkId());
        setName(model.name());
        setArn(getArnFormat());

        getSecurityGroups().clear();
        if (model.hasSecurityGroupIds()) {
            setSecurityGroups(model.securityGroupIds()
                .stream()
                .map(r -> findById(SecurityGroupResource.class, r))
                .collect(Collectors.toList()));
        }

        getSubnets().clear();
        if (model.hasSubnetIds()) {
            setSubnets(model.subnetIds()
                .stream()
                .map(r -> findById(SubnetResource.class, r))
                .collect(Collectors.toList()));
        }
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        VpcLink vpcLink = getVpcLink(client);

        if (vpcLink == null) {
            return false;
        }

        copyFrom(vpcLink);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateVpcLinkResponse vpcLink = client.createVpcLink(r -> r.name(getName())
            .securityGroupIds(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .tags(getTags()));

        setId(vpcLink.vpcLinkId());
        setArn(getArnFormat());

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getVpcLink(client).vpcLinkStatus().equals(VpcLinkStatus.AVAILABLE));
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateVpcLink(r -> r.name(getName()).vpcLinkId(getId()));

        if (changedFieldNames.contains("tags")) {
            DomainNameResource currentResource = (DomainNameResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(r -> r.resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(r -> r.resourceArn(getArn()).tags(getTags()));
        }

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getVpcLink(client).vpcLinkStatus().equals(VpcLinkStatus.AVAILABLE));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteVpcLink(r -> r.vpcLinkId(getId()));
    }

    private VpcLink getVpcLink(ApiGatewayV2Client client) {
        VpcLink vpcLink = null;

        GetVpcLinksResponse response = client.getVpcLinks(GetVpcLinksRequest.builder().build());

        if (response.hasItems()) {
            vpcLink = response.items().stream().filter(i -> i.vpcLinkId().equals(getId())).findFirst().orElse(null);
        }

        return vpcLink;
    }

    private String getArnFormat() {
        return String.format("arn:aws:apigateway:%s::/vpclinks/%s", credentials(AwsCredentials.class).getRegion(),
            getId());
    }
}
