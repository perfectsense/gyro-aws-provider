/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateOpenIdConnectProviderResponse;
import software.amazon.awssdk.services.iam.model.GetOpenIdConnectProviderResponse;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;

/**
 * Creates an Open Id connect provider.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::iam-open-id-connect-provider provider-example
 *         client-ids : [ "sts.amazon.com" ]
 *         thumb-prints : [ "9e99a48a9960b14926cc7f3b02e22da2b0ab7280" ]
 *         url : "https://oidc.eks.us-east-2.amazonaws.com/id/50B4045A6F925CDC0F497A99C624"
 *     end
 */
@Type("iam-open-id-connect-provider")
public class OpenIdConnectProviderResource extends AwsResource implements Copyable<OpenIdConnectProviderCustom> {

    private List<String> clientIds;
    private List<String> thumbPrints;
    private String url;
    private String createdDate;
    private String arn;

    /**
     * A list of client id's (also knows as audiences) for the the open id connect provider.
     */
    @Required
    public List<String> getClientIds() {
        if (clientIds == null) {
            clientIds = new ArrayList<>();
        }

        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    /**
     * A list of server certificate thumbprints for the the open id connect provider. See `Obtaining the providers thumbprint <https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_create_oidc_verify-thumbprint.html>`_.
     */
    @Required
    @Updatable
    public List<String> getThumbPrints() {
        if (thumbPrints == null) {
            thumbPrints = new ArrayList<>();
        }

        return thumbPrints;
    }

    public void setThumbPrints(List<String> thumbPrints) {
        this.thumbPrints = thumbPrints;
    }

    /**
     * The url for an identity provider.
     */
    @Required
    @Regex(value = "^(https)://.+$", message = "a string starting with 'https://'")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The creation date the open id connect provider.
     */
    @Output
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * The arn for the open id connect provider.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public boolean refresh() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        try {
            GetOpenIdConnectProviderResponse response = client.getOpenIDConnectProvider(r -> r.openIDConnectProviderArn(
                getArn()));

            copyFrom(response);
            return true;
        } catch (NoSuchEntityException ex) {
            // not found
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        CreateOpenIdConnectProviderResponse response = client.createOpenIDConnectProvider(r ->
            r.clientIDList(getClientIds())
                .thumbprintList(getThumbPrints())
                .url(getUrl()));

        setArn(response.openIDConnectProviderArn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        client.updateOpenIDConnectProviderThumbprint(r -> r.openIDConnectProviderArn(getArn()).thumbprintList(getThumbPrints()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        client.deleteOpenIDConnectProvider(r -> r.openIDConnectProviderArn(getArn()));
    }

    @Override
    public void copyFrom(OpenIdConnectProviderCustom model) {
        copyFrom(model.getProvider());
        setArn(model.getArn());
    }

    public void copyFrom(GetOpenIdConnectProviderResponse model) {
        setClientIds(model.clientIDList());
        setThumbPrints(model.thumbprintList());
        setUrl(String.format("https://%s", model.url()));
        setCreatedDate(model.createDate().toString());
    }
}
