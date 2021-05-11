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

package gyro.aws.eks;

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.OidcIdentityProviderConfig;
import software.amazon.awssdk.services.eks.model.OidcIdentityProviderConfigRequest;

public class OidcIdentityProviderConfiguration extends Diffable implements Copyable<OidcIdentityProviderConfig> {

    private String clientId;
    private String groupsClaim;
    private String groupsPrefix;
    private String arn;
    private String configName;
    private String issuerUrl;
    private String status;
    private Map<String, String> requiredClaims;
    private String usernameClaim;
    private String usernamePrefix;

    /**
     * The id of the client that the makes authentication request.
     */
    @Required
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * The claim that the JWT token that the oidc provider uses to return groups.
     */
    public String getGroupsClaim() {
        return groupsClaim;
    }

    public void setGroupsClaim(String groupsClaim) {
        this.groupsClaim = groupsClaim;
    }

    /**
     * A prefix prepended to group claims to avoid naming conflicts.
     */
    public String getGroupsPrefix() {
        return groupsPrefix;
    }

    public void setGroupsPrefix(String groupsPrefix) {
        this.groupsPrefix = groupsPrefix;
    }

    /**
     * The arn of the identity provider configuration
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The name of the identity provider configuration.
     */
    @Required
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * The url of the identity provider that allows the discovery of public signed keys for authentication to EKS.
     */
    @Required
    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    /**
     * Status of the identity provider.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * A key value pair that determines required claims apart from the group and user claims.
     */
    public Map<String, String> getRequiredClaims() {
        if (requiredClaims == null) {
            requiredClaims = new HashMap<>();
        }

        return requiredClaims;
    }

    public void setRequiredClaims(Map<String, String> requiredClaims) {
        this.requiredClaims = requiredClaims;
    }

    /**
     * The claim that the JWT token that the oidc provider uses to return users.
     */
    public String getUsernameClaim() {
        return usernameClaim;
    }

    public void setUsernameClaim(String usernameClaim) {
        this.usernameClaim = usernameClaim;
    }

    /**
     * A prefix prepended to user claims to avoid naming conflicts.
     */
    public String getUsernamePrefix() {
        return usernamePrefix;
    }

    public void setUsernamePrefix(String usernamePrefix) {
        this.usernamePrefix = usernamePrefix;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(OidcIdentityProviderConfig model) {
        setClientId(model.clientId());
        setGroupsClaim(model.groupsClaim());
        setGroupsPrefix(model.groupsPrefix());
        setArn(model.identityProviderConfigArn());
        setConfigName(model.identityProviderConfigName());
        setIssuerUrl(model.issuerUrl());
        setRequiredClaims(model.requiredClaims());
        setStatus(model.statusAsString());
        setUsernameClaim(model.usernameClaim());
        setUsernamePrefix(model.usernamePrefix());
    }

    protected OidcIdentityProviderConfigRequest toOidcIdentityProviderConfig() {
        return OidcIdentityProviderConfigRequest.builder()
            .identityProviderConfigName(getConfigName())
            .clientId(getClientId())
            .issuerUrl(getIssuerUrl())
            .groupsClaim(getGroupsClaim())
            .groupsPrefix(getGroupsPrefix())
            .usernameClaim(getUsernameClaim())
            .usernamePrefix(getUsernamePrefix())
            .build();
    }
}
