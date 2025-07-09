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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ssm.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;
import software.amazon.awssdk.services.ssm.model.ParameterTier;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.ResourceTypeForTagging;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.model.Tag;

/**
 * Creates an SSM Parameter.
 *
 * Example
 *
 * .. code-block:: gyro
 *
 *    aws::ssm-parameter ssm-parameter-example
 *        parameter-name: "/example/parameter"
 *        description: "An example parameter"
 *        value: "example-value"
 *        type: STRING
 *        allowed-pattern: ".*"
 *        tier: STANDARD
 *        tags: {
 *            Environment: "Test"
 *            Project: "Example"
 *        }
 *    end
 */
@Type("ssm-parameter")
public class SsmParameterResource extends AwsResource implements Copyable<ParameterMetadata> {

    private String parameterName;
    private String description;
    private String value;
    private ParameterType type;
    private KmsKeyResource key;
    private String allowedPattern;
    private ParameterTier tier;
    private List<ParameterPolicy> policy;
    private String dataType;
    private Map<String, String> tags;

    // Read-only
    private String arn;
    private Long version;

    /**
     * The name of the parameter.
     */
    @Required
    @Regex("^[a-zA-Z0-9_.\\-\\/]+$")
    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * The description of the parameter.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The value of the parameter.
     */
    @Required
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of the parameter.
     */
    @Required
    @ValidStrings({ "STRING", "STRING_LIST", "SECURE_STRING" })
    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    /**
     * The KMS Key for SecureString type.
     */
    @Updatable
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    /**
     * The regular expression used to validate the parameter value.
     */
    @Updatable
    public String getAllowedPattern() {
        return allowedPattern;
    }

    public void setAllowedPattern(String allowedPattern) {
        this.allowedPattern = allowedPattern;
    }

    /**
     * The tier to assign to the parameter.
     */
    @Updatable
    @ValidStrings({ "STANDARD", "ADVANCED", "INTELLIGENT_TIERING" })
    public ParameterTier getTier() {
        return tier;
    }

    public void setTier(ParameterTier tier) {
        this.tier = tier;
    }

    /**
     * The policies to assign to the parameter.
     */
    @Updatable
    public List<ParameterPolicy> getPolicy() {
        if (policy == null) {
            policy = new java.util.ArrayList<>();
        }

        return policy;
    }

    public void setPolicy(List<ParameterPolicy> policy) {
        this.policy = policy;
    }

    /**
     * The data type of the parameter.
     */
    @ValidStrings({ "text", "aws:ec2:image", "aws:ssm:integration" })
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * The tags to assign to the parameter.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ARN of the parameter.
     */
    @Output
    @Id
    public String getArn() {
        return this.arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The version of the parameter.
     */
    @Output
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public void copyFrom(ParameterMetadata model) {
        setParameterName(model.name());
        setDescription(model.description());
        setType(model.type());
        setDataType(model.dataType());
        setTier(model.tier());
        setArn(model.arn());
        setAllowedPattern(model.allowedPattern());
        setVersion(model.version());

        // refresh key
        String keyId = model.keyId();
        if (keyId != null) {
            if (keyId.startsWith("arn:aws:kms")) {
                setKey(findById(KmsKeyResource.class, keyId));
            } else {
                findByClass(KmsKeyResource.class).filter(
                        r -> keyId.equals(r.getId()) || (r.getAliases() != null && r.getAliases().contains(keyId)))
                    .findFirst()
                    .ifPresent(this::setKey);
            }
        }

        // refresh policy
        getPolicy().clear();
        if (model.hasPolicies()) {
            model.policies().forEach(p -> {
                ParameterPolicy parameterPolicy = new ParameterPolicy();
                parameterPolicy.copyFrom(p);
                getPolicy().add(parameterPolicy);
            });
        }

        SsmClient client = createClient(SsmClient.class);

        // refresh value
        setValue(null);
        GetParameterResponse response = client.getParameter(
            GetParameterRequest.builder().name(getParameterName()).withDecryption(true).build());
        if (response != null && response.parameter() != null) {
            setValue(response.parameter().value());
        }

        // refresh tags
        getTags().clear();
        ListTagsForResourceResponse tagsResponse =
            client.listTagsForResource(ListTagsForResourceRequest.builder().resourceId(getArn()).resourceType(
                ResourceTypeForTagging.PARAMETER).build());
        if (tagsResponse.hasTagList()) {
            getTags().putAll(
                tagsResponse.tagList().stream().collect(java.util.stream.Collectors.toMap(Tag::key, Tag::value)));
        }
    }

    @Override
    public boolean refresh() {
        SsmClient client = createClient(SsmClient.class);

        try {

            ParameterMetadata metadata = describeParameter(client);

            if (metadata != null) {
                copyFrom(metadata);

                return true;
            }

        } catch (SsmException ex) {
            // Ignore
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        SsmClient client = createClient(SsmClient.class);
        PutParameterResponse response = client.putParameter(PutParameterRequest.builder()
            .name(getParameterName())
            .description(getDescription())
            .value(getValue())
            .type(getType())
            .keyId(getKey() == null ? null : getKey().getArn())
            .allowedPattern(getAllowedPattern())
            .tier(getTier())
            .policies(ParameterPolicy.toSdkParameterPolicy(getPolicy()))
            .dataType(getDataType())
            .build());

        setVersion(response.version());
        ParameterMetadata metadata = describeParameter(client);
        if (metadata != null) {
            setArn(metadata.arn());
        }

        state.save();

        if (!getTags().isEmpty()) {
            client.addTagsToResource(r -> r.resourceId(getArn())
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tags(getTags().entrySet().stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toSet())));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        SsmClient client = createClient(SsmClient.class);

        if (changedFieldNames.stream().anyMatch(r -> !r.equals("tags"))) {
            PutParameterRequest.Builder requestBuilder = PutParameterRequest.builder()
                .name(getParameterName())
                .overwrite(true)
                .description(getDescription())
                .value(getValue())
                .keyId(getKey() == null ? null : getKey().getArn())
                .allowedPattern(getAllowedPattern())
                .tier(getTier())
                .policies(ParameterPolicy.toSdkParameterPolicy(getPolicy()));

            client.putParameter(requestBuilder.build());
        }

        state.save();

        if (changedFieldNames.contains("tags")) {
            // Remove existing tags
            client.removeTagsFromResource(r -> r.resourceId(getArn())
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tagKeys(getTags().keySet()));

            // Add new tags
            client.addTagsToResource(r -> r.resourceId(getArn())
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .tags(getTags().entrySet().stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toSet())));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        SsmClient client = createClient(SsmClient.class);
        client.deleteParameter(DeleteParameterRequest.builder().name(getParameterName()).build());
    }

    private ParameterMetadata describeParameter(SsmClient client) {
        DescribeParametersResponse response = client.describeParameters(DescribeParametersRequest.builder()
            .parameterFilters(
                ParameterStringFilter.builder().key("Name").option("Equals").values(getParameterName()).build())
            .build());

        return response != null && response.hasParameters() && !response.parameters().isEmpty() ?
            response.parameters().get(0) : null;
    }
}
