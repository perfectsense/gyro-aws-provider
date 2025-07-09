/*
 * Copyright 2025, Brightspot, Inc.
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

package gyro.aws.ssm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ssm.model.ParameterInlinePolicy;
import software.amazon.awssdk.utils.IoUtils;

public class ParameterPolicy extends Diffable implements Copyable<ParameterInlinePolicy> {

    private String policyText;

    // Read-only
    private String policyType;
    private String policyStatus;

    /**
     * The policy text of the parameter.
     */
    @Updatable
    @Required
    public String getPolicyText() {
        if (this.policyText != null && this.policyText.contains(".json")) {
            try (InputStream input = openInput(this.policyText)) {
                this.policyText = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.policyText;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return this.policyText;
        }
    }

    public void setPolicyText(String policyText) {
        this.policyText = policyText;
    }

    /**
     * The type of policy. Parameter Store, a tool in AWS Systems Manager
     */
    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    /**
     * The status of the policy. Policies report the following statuses: ``Pending`` (the policy hasn't been enforced or applied yet), ``Finished`` (the policy was applied), ``Failed`` (the policy wasn't applied), or `InProgress`` (the policy is being applied now).
     */
    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    @Override
    public void copyFrom(ParameterInlinePolicy model) {
        setPolicyText(model.policyText());
        setPolicyType(model.policyType());
        setPolicyStatus(model.policyStatus());
    }

    @Override
    public String primaryKey() {
        return getPolicyText();
    }

    public static String toSdkParameterPolicy(List<ParameterPolicy> policies) throws JsonProcessingException {
        if (policies == null || policies.isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        policies.stream().filter(Objects::nonNull).filter(r -> r.getPolicyText() != null)
            .map(ParameterPolicy::getPolicyText).forEach(r -> {
                try {
                    JsonNode node = mapper.readTree(r);
                    arrayNode.add(node);
                } catch (JsonProcessingException e) {
                    throw new GyroException(String.format("Invalid JSON policy text: <%s>", r), e);
                }

            });

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
    }
}
