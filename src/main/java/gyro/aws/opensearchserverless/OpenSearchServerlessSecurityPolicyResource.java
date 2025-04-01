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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.CreateSecurityPolicyResponse;
import software.amazon.awssdk.services.opensearchserverless.model.GetSecurityPolicyResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityPolicyDetail;
import software.amazon.awssdk.services.opensearchserverless.model.SecurityPolicyType;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Create an OpenSearch Serverless security policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-serverless-security-policy security-policy
 *         name: "example-security-policy"
 *         description: "example-security-policy-description"
 *         type: "encryption"
 *         policy: "encryption-policy.json"
 *     end
 */
@Type("opensearch-serverless-security-policy")
public class OpenSearchServerlessSecurityPolicyResource extends AwsResource implements Copyable<SecurityPolicyDetail> {

    private String name;
    private String description;
    private SecurityPolicyType type;
    private String policy;
    private String policyVersion;

    /**
     * The name of the security policy.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the security policy.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The type of the security policy.
     */
    @Required
    @ValidStrings({ "network", "encryption" })
    public SecurityPolicyType getType() {
        return type;
    }

    public void setType(SecurityPolicyType type) {
        this.type = type;
    }

    /**
     * The policy of the security policy. A policy path or policy string is allowed.
     */
    @Required
    @Updatable
    public String getPolicy() {
        if (policy != null && policy.contains(".json")) {
            try (InputStream input = openInput(policy)) {
                policy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return policy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(policy);
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The policy version of the security policy.
     */
    @Updatable
    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    @Override
    public void copyFrom(SecurityPolicyDetail model) {
        setName(model.name());
        setDescription(model.description());
        setType(model.type());
        setPolicy(model.policy().toString());
        setPolicyVersion(model.policyVersion());
    }

    @Override
    public boolean refresh() {
        try {
            OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
            GetSecurityPolicyResponse response = client.getSecurityPolicy(r -> r.name(getName())
                .type(getType())
                .build());

            copyFrom(response.securityPolicyDetail());

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
        CreateSecurityPolicyResponse response = client.createSecurityPolicy(r -> r.clientToken(token)
            .description(getDescription())
            .name(getName())
            .policy(getPolicy())
            .type(getType())
        );

        copyFrom(response.securityPolicyDetail());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
        String token = UUID.randomUUID().toString();
        client.updateSecurityPolicy(r -> r.clientToken(token)
            .name(getName())
            .policy(getPolicy())
            .type(getType())
            .description(getDescription())
            .policyVersion(getPolicyVersion())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try {
            OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class);
            String token = UUID.randomUUID().toString();
            client.deleteSecurityPolicy(r -> r.clientToken(token).name(getName()).type(getType()));
        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }
}
