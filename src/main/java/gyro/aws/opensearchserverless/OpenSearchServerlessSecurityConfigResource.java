/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearchserverless;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.CreateSecurityConfigRequest;
import software.amazon.awssdk.services.opensearchserverless.model.CreateSecurityConfigResponse;
import software.amazon.awssdk.services.opensearchserverless.model.GetSecurityConfigResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityConfigDetail;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityConfigType;
import software.amazon.awssdk.services.opensearchserverless.model.UpdateSecurityConfigRequest;

/**
 * Creates an OpenSearch Serverless security configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-serverless-security-config example-security-config
 *         name: "example-security-config"
 *         description: "example-security-config"
 *         type: "saml"
 *         saml-config
 *             group-attribute: "group-attribute"
 *             metadata: "metadata"
 *             session-timeout: 60
 *             user-attribute: "user-attribute"
 *         end
 *     end
 */
@Type("opensearch-serverless-security-config")
public class OpenSearchServerlessSecurityConfigResource extends AwsResource implements Copyable<SecurityConfigDetail> {

    private String name;
    private String description;
    private SecurityConfigType type;
    private String configVersion;
    private OpenSearchServerlessSamlConfig samlConfig;
    private OpenSearchServerlessIamIdentityCenterConfig iamIdentityCenterConfig;
    private String id;

    /**
     * The name of the security configuration.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the security configuration.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The type of the security configuration.
     */
    @Required
    @ValidStrings({ "saml", "iamidentitycenter" })
    public SecurityConfigType getType() {
        return type;
    }

    public void setType(SecurityConfigType type) {
        this.type = type;
    }

    /**
     * The version of the security configuration.
     */
    @Updatable
    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * The SAML configuration for the security configuration.
     *
     * @subresource gyro.aws.opensearchserverless.OpenSearchServerlessSamlConfig
     */
    @Updatable
    @DependsOn("type")
    @ConflictsWith("iam-identity-center-config")
    public OpenSearchServerlessSamlConfig getSamlConfig() {
        return samlConfig;
    }

    public void setSamlConfig(OpenSearchServerlessSamlConfig samlConfig) {
        this.samlConfig = samlConfig;
    }

    /**
     * The IAM Identity Center configuration for the security configuration.
     *
     * @subresource gyro.aws.opensearchserverless.OpenSearchServerlessIamIdentityCenterConfig
     */
    @Updatable
    @DependsOn("type")
    @ConflictsWith("saml-config")
    public OpenSearchServerlessIamIdentityCenterConfig getIamIdentityCenterConfig() {
        return iamIdentityCenterConfig;
    }

    public void setIamIdentityCenterConfig(OpenSearchServerlessIamIdentityCenterConfig iamIdentityCenterConfig) {
        this.iamIdentityCenterConfig = iamIdentityCenterConfig;
    }

    /**
     * The ID of the security configuration.
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
    public void copyFrom(SecurityConfigDetail model) {
        setDescription(model.description());
        setType(model.type());
        setConfigVersion(model.configVersion());
        setId(model.id());

        setSamlConfig(null);
        if (model.samlOptions() != null) {
            OpenSearchServerlessSamlConfig newSamlConfig = newSubresource(OpenSearchServerlessSamlConfig.class);
            newSamlConfig.copyFrom(model.samlOptions());
            setSamlConfig(newSamlConfig);
        }

        setIamIdentityCenterConfig(null);
        if (model.iamIdentityCenterOptions() != null) {
            OpenSearchServerlessIamIdentityCenterConfig newIamIdentityCenterConfig = newSubresource(
                OpenSearchServerlessIamIdentityCenterConfig.class);
            newIamIdentityCenterConfig.copyFrom(model.iamIdentityCenterOptions());
            setIamIdentityCenterConfig(newIamIdentityCenterConfig);
        }
    }

    @Override
    public boolean refresh() {
        try {
            OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);

            GetSecurityConfigResponse response = client.getSecurityConfig(r -> r.id(getId()));

            copyFrom(response.securityConfigDetail());

            return true;

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        String token = UUID.randomUUID().toString();
        CreateSecurityConfigRequest.Builder builder = CreateSecurityConfigRequest.builder()
            .clientToken(token)
            .description(getDescription())
            .name(getName())
            .type(getType());

        if (getSamlConfig() != null) {
            builder = builder.samlOptions(getSamlConfig().toSamlConfigOptions());
        }

        if (getIamIdentityCenterConfig() != null) {
            builder =
                builder.iamIdentityCenterOptions(
                    getIamIdentityCenterConfig().toIamIdentityCenterConfigOptionsCreate());
        }

        CreateSecurityConfigResponse response = client.createSecurityConfig(builder.build());

        copyFrom(response.securityConfigDetail());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        String token = UUID.randomUUID().toString();
        UpdateSecurityConfigRequest.Builder builder = UpdateSecurityConfigRequest.builder()
            .clientToken(token)
            .id(getId())
            .description(getDescription())
            .configVersion(getConfigVersion());

        if (changedFieldNames.contains("saml-config")) {
            builder = builder.samlOptions(getSamlConfig().toSamlConfigOptions());
        }

        if (changedFieldNames.contains("iam-identity-center-config")) {
            builder = builder.iamIdentityCenterOptionsUpdates(getIamIdentityCenterConfig()
                .toIamIdentityCenterConfigOptionsUpdate());
        }

        client.updateSecurityConfig(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try {
            OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
            String token = UUID.randomUUID().toString();
            client.deleteSecurityConfig(r -> r.id(getId()).clientToken(token));

        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getType() != null) {
            if (SecurityConfigType.SAML.equals(getType()) && getSamlConfig() == null) {
                errors.add(new ValidationError(this, null, "SAML configuration is required for 'saml' type."));
            }

            if (SecurityConfigType.IAMIDENTITYCENTER.equals(getType()) && getIamIdentityCenterConfig() == null) {
                errors.add(new ValidationError(
                    this,
                    null,
                    "IAM Identity Center configuration is required for 'iamidentitycenter' type."));
            }
        }

        return errors;
    }
}
