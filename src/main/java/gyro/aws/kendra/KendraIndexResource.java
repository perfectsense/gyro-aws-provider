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

package gyro.aws.kendra;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.CreateIndexRequest;
import software.amazon.awssdk.services.kendra.model.CreateIndexResponse;
import software.amazon.awssdk.services.kendra.model.DescribeIndexResponse;
import software.amazon.awssdk.services.kendra.model.IndexEdition;
import software.amazon.awssdk.services.kendra.model.IndexStatus;
import software.amazon.awssdk.services.kendra.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kendra.model.Tag;
import software.amazon.awssdk.services.kendra.model.TagResourceRequest;
import software.amazon.awssdk.services.kendra.model.UntagResourceRequest;
import software.amazon.awssdk.services.kendra.model.UpdateIndexRequest;

/**
 * Creates a Kendra Index.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kendra-index index-example-enter
 *         description: "example-index-desc"
 *         edition: ENTERPRISE_EDITION
 *         name: "example-index-1-change"
 *         role: "arn:aws:iam::242040583208:role/service-role/AmazonKendra-us-east-1-example-role-har"
 *
 *         capacity-units-configuration
 *             query-capacity-units: 5
 *             storage-capacity-units: 1
 *         end
 *
 *         tags: {
 *             "example-key-1": "example-value-1",
 *             "example-key-2": "example-value-2"
 *         }
 *     end
 */
@Type("kendra-index")
public class KendraIndexResource extends AwsResource implements Copyable<DescribeIndexResponse> {

    private String description;
    private IndexEdition edition;
    private String name;
    private RoleResource role;
    private KendraServerSideEncryptionConfiguration serverSideEncryptionConfig;
    private KendraCapacityUnitsConfiguration capacityUnitsConfiguration;
    private Map<String, String> tags;

    // Output
    private String id;
    private String arn;

    /**
     * The description for the index.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Amazon Kendra edition to use for the index. Valid values are ``DEVELOPER_EDITION`` or ``ENTERPRISE_EDITION``.
     */
    public IndexEdition getEdition() {
        return edition;
    }

    public void setEdition(IndexEdition edition) {
        this.edition = edition;
    }

    /**
     * The name of the index. (Required)
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The IAM role that gives Amazon Kendra permissions to access Amazon CloudWatch logs and metrics. (Required)
     */
    @Required
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The encryption configuration for the index.
     */
    public KendraServerSideEncryptionConfiguration getServerSideEncryptionConfig() {
        return serverSideEncryptionConfig;
    }

    public void setServerSideEncryptionConfig(KendraServerSideEncryptionConfiguration serverSideEncryptionConfig) {
        this.serverSideEncryptionConfig = serverSideEncryptionConfig;
    }

    /**
     * The number of addtional storage and query capacity units that should be used by the index.
     */
    @Updatable
    public KendraCapacityUnitsConfiguration getCapacityUnitsConfiguration() {
        return capacityUnitsConfiguration;
    }

    public void setCapacityUnitsConfiguration(KendraCapacityUnitsConfiguration capacityUnitsConfiguration) {
        this.capacityUnitsConfiguration = capacityUnitsConfiguration;
    }

    /**
     *The tags associated with the index.
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
     * The ID of the index.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ARN of the index.
     */
    @Output
    private String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(DescribeIndexResponse model) {
        setName(model.name());
        setDescription(model.description());
        setEdition(model.edition());
        setRole(findById(RoleResource.class, model.roleArn()));
        setId(model.id());
        setArn(getArnFormat());

        if (model.capacityUnits() != null) {
            KendraCapacityUnitsConfiguration capacityUnits = newSubresource(KendraCapacityUnitsConfiguration.class);
            capacityUnits.copyFrom(model.capacityUnits());
            setCapacityUnitsConfiguration(capacityUnits);
        }

        if (model.serverSideEncryptionConfiguration() != null) {
            KendraServerSideEncryptionConfiguration encryptionConfig = newSubresource(
                KendraServerSideEncryptionConfiguration.class);
            encryptionConfig.copyFrom(model.serverSideEncryptionConfiguration());
            setServerSideEncryptionConfig(encryptionConfig);
        }

        getTags().clear();
        KendraClient client = createClient(KendraClient.class);
        client.listTagsForResource(r -> r.resourceARN(getArn())).tags().forEach(t -> getTags().put(t.key(), t.value()));
    }

    @Override
    public boolean refresh() {
        KendraClient client = createClient(KendraClient.class);

        DescribeIndexResponse index = getIndex(client);

        if (index == null) {
            return false;
        }

        copyFrom(index);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        CreateIndexRequest.Builder builder = CreateIndexRequest.builder()
            .name(getName())
            .roleArn(getRole().getArn())
            .description(getDescription())
            .edition(getEdition())
            .tags(getTags().entrySet().stream().map(e -> Tag.builder().key(e.getKey())
                .value(e.getValue()).build()).collect(Collectors.toList()));

        if (getServerSideEncryptionConfig() != null) {
            builder = builder.serverSideEncryptionConfiguration(getServerSideEncryptionConfig().toServerSideEncryptionConfiguration());
        }

        CreateIndexResponse index = client.createIndex(builder.build());
        setId(index.id());
        setArn(getArnFormat());

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getIndex(client).status().equals(IndexStatus.ACTIVE));

        state.save();

        if (getCapacityUnitsConfiguration() != null) {
            client.updateIndex(r -> r.id(getId())
                .capacityUnits(getCapacityUnitsConfiguration().toCapacityUnitsConfiguration()));

            Wait.atMost(30, TimeUnit.MINUTES)
                .checkEvery(5, TimeUnit.MINUTES)
                .prompt(false)
                .until(() -> getIndex(client).status().equals(IndexStatus.ACTIVE));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        UpdateIndexRequest.Builder builder = UpdateIndexRequest.builder().id(getId());

        if (changedFieldNames.contains("name")) {
            builder = builder.name(getName());
        }

        if (changedFieldNames.contains("role")) {
            builder = builder.roleArn(getRole().getArn());
        }

        if (changedFieldNames.contains("description")) {
            builder = builder.description(getDescription());
        }

        if (changedFieldNames.contains("capacity-units-configuration")) {
            builder = builder.capacityUnits(getCapacityUnitsConfiguration() == null ? null
                : getCapacityUnitsConfiguration().toCapacityUnitsConfiguration());
        }

        client.updateIndex(builder.build());

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getIndex(client).status().equals(IndexStatus.ACTIVE));

        if (changedFieldNames.contains("tags")) {
            KendraIndexResource currentResource = (KendraIndexResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceARN(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(TagResourceRequest.builder()
                .resourceARN(getArn())
                .tags(getTags().entrySet()
                    .stream()
                    .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                    .collect(Collectors.toList()))
                .build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        client.deleteIndex(r -> r.id(getId()));

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getIndex(client) == null);
    }

    private DescribeIndexResponse getIndex(KendraClient client) {
        DescribeIndexResponse index = null;

        try {
            index = client.describeIndex(r -> r.id(getId()));

        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return index;
    }

    private String getArnFormat() {
        return String.format("arn:aws:kendra:%s:%s:index/%s", credentials(AwsCredentials.class).getRegion(),
            getRole().getArn().split(":")[4], getId());
    }
}
