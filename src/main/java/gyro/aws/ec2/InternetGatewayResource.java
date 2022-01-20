/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.ec2;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateInternetGatewayResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.InternetGateway;

/**
 * Create an internet gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::internet-gateway example-gateway
 *         vpc: $(aws::vpc vpc-example)
 *     end
 */
@Type("internet-gateway")
public class InternetGatewayResource extends Ec2TaggableResource<InternetGateway> implements Copyable<InternetGateway> {

    private String id;
    private VpcResource vpc;

    /**
     * The VPC to create an internet gateway in.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The ID of the Internet Gateway.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(InternetGateway internetGateway) {
        setId(internetGateway.internetGatewayId());

        if (!internetGateway.attachments().isEmpty() && !ObjectUtils.isBlank(internetGateway.attachments().get(0).vpcId())) {
            setVpc(findById(VpcResource.class, internetGateway.attachments().get(0).vpcId()));
        } else {
            setVpc(null);
        }

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        InternetGateway internetGateway = getInternetGateway(client);

        if (internetGateway == null) {
            return false;
        }

        copyFrom(internetGateway);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateInternetGatewayResponse response = client.createInternetGateway();

        setId(response.internetGateway().internetGatewayId());

        state.save();

        Wait.atMost(10, TimeUnit.SECONDS)
            .checkEvery(2, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> getInternetGateway(client) != null);

        if (getVpc() != null) {
            client.attachInternetGateway(r -> r.internetGatewayId(getId())
                    .vpcId(getVpc().getId())
            );
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        InternetGateway internetGateway = getInternetGateway(client);

        if (internetGateway != null && !internetGateway.attachments().isEmpty()) {
            Wait.atMost(1, TimeUnit.MINUTES)
                .checkEvery(2, TimeUnit.SECONDS)
                .resourceOverrides(this, TimeoutSettings.Action.DELETE)
                .prompt(false)
                .until(() -> {
                    try {
                        client.detachInternetGateway(
                            r -> r.internetGatewayId(getId()).vpcId(internetGateway.attachments().get(0).vpcId())
                        );
                    } catch (Ec2Exception e) {
                        // DependencyViolation should be retried since this resource may be waiting for a
                        // previously deleted resource to finish deleting.
                        if ("DependencyViolation".equals(e.awsErrorDetails().errorCode())) {
                            return false;
                        }
                    }

                    return true;
                }
            );
        }

        client.deleteInternetGateway(r -> r.internetGatewayId(getId()));
    }

    private InternetGateway getInternetGateway(Ec2Client client) {
        InternetGateway internetGateway = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load internet gateway.");
        }

        try {
            DescribeInternetGatewaysResponse response = client.describeInternetGateways(
                r -> r.internetGatewayIds(getId())
            );

            if (!response.internetGateways().isEmpty()) {
                internetGateway = response.internetGateways().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return internetGateway;
    }
}
