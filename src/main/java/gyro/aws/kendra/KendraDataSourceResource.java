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
import software.amazon.awssdk.services.kendra.model.CreateDataSourceResponse;
import software.amazon.awssdk.services.kendra.model.DataSourceStatus;
import software.amazon.awssdk.services.kendra.model.DataSourceSyncJob;
import software.amazon.awssdk.services.kendra.model.DataSourceSyncJobStatus;
import software.amazon.awssdk.services.kendra.model.DataSourceType;
import software.amazon.awssdk.services.kendra.model.DescribeDataSourceResponse;
import software.amazon.awssdk.services.kendra.model.ListDataSourceSyncJobsResponse;
import software.amazon.awssdk.services.kendra.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kendra.model.Tag;
import software.amazon.awssdk.services.kendra.model.TagResourceRequest;
import software.amazon.awssdk.services.kendra.model.UntagResourceRequest;

/**
 * Creates a VPC with the specified IPv4 CIDR block.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kendra-data-source data-source-example-s3
 *         name: "s3-ex"
 *         description: "s3-desc"
 *         index: $(aws::kendra-index index-example-enter)
 *         role: "arn:aws:iam::242040583208:role/service-role/AmazonKendra-s3"
 *         schedule: 'cron(0 11 * * ? *)'
 *         type: S3
 *
 *         configuration
 *             s3-configuration
 *                 bucket: "example-kendra"
 *
 *                 exclusion-patterns: [
 *                     "example_pattern"
 *                 ]
 *
 *                 inclusion-prefixes: [
 *                     "example_pattern"
 *                 ]
 *             end
 *         end
 *
 *         tags: {
 *             "example-key-1": "example-value-1"
 *         }
 *     end
 */
@Type("kendra-data-source")
public class KendraDataSourceResource extends AwsResource implements Copyable<DescribeDataSourceResponse> {

    private String name;
    private String description;
    private KendraIndexResource index;
    private RoleResource role;
    private String schedule;
    private DataSourceType type;
    private KendraDataSourceConfiguration configuration;
    private Map<String, String> tags;

    // Output
    private String id;

    /**
     * The name of the data source.
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
     * The description of the data source.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The index that should be associated with this data source.
     */
    @Required
    public KendraIndexResource getIndex() {
        return index;
    }

    public void setIndex(KendraIndexResource index) {
        this.index = index;
    }

    /**
     * The role with permission to access the data source.
     */
    @Updatable
    @Required
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The frequency that Amazon Kendra will check the documents in your repository and update the index.
     */
    @Updatable
    @Required
    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * The type of repository that contains the data source.
     */
    @Required
    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    /**
     * The data source connector configuration information required to access the repository.
     */
    @Updatable
    @Required
    public KendraDataSourceConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(KendraDataSourceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * The tags associated with the data source.
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
     * The id of the data source.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(DescribeDataSourceResponse model) {
        setId(model.id());
        setIndex(findById(KendraIndexResource.class, model.indexId()));
        setName(model.name());
        setRole(findById(RoleResource.class, model.roleArn()));
        setDescription(model.description());
        setSchedule(model.schedule());
        setType(model.type());

        KendraDataSourceConfiguration config = newSubresource(KendraDataSourceConfiguration.class);
        config.copyFrom(model.configuration());
        setConfiguration(config);

        getTags().clear();
        KendraClient client = createClient(KendraClient.class);
        client.listTagsForResource(r -> r.resourceARN(getArn())).tags().forEach(t -> getTags().put(t.key(), t.value()));
    }

    @Override
    public boolean refresh() {
        KendraClient client = createClient(KendraClient.class);

        DescribeDataSourceResponse dataSource = getDataSource(client);

        if (dataSource == null) {
            return false;
        }

        copyFrom(dataSource);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        CreateDataSourceResponse dataSource = client.createDataSource(r -> r
            .configuration(getConfiguration().toDataSourceConfiguration())
            .description(getDescription())
            .indexId(getIndex().getId())
            .name(getName())
            .roleArn(getRole().getArn())
            .schedule(getSchedule())
            .type(getType())
            .tags(getTags().entrySet().stream().map(e -> Tag.builder().key(e.getKey())
                .value(e.getValue()).build()).collect(Collectors.toList()))
        );

        setId(dataSource.id());
        state.save();

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getDataSource(client).status().equals(DataSourceStatus.ACTIVE) && !isSyncing(client));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        client.updateDataSource(r -> r.id(getId()).name(getName()).indexId(getIndex().getId())
            .description(getDescription()).roleArn(getRole().getArn()).schedule(getSchedule())
            .configuration(getConfiguration().toDataSourceConfiguration())
        );

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getDataSource(client).status().equals(DataSourceStatus.ACTIVE) && !isSyncing(client));

        if (changedFieldNames.contains("tags")) {
            KendraDataSourceResource currentResource = (KendraDataSourceResource) current;

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

        client.deleteDataSource(r -> r.id(getId()).indexId(getIndex().getId()));

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getDataSource(client) == null);
    }

    private DescribeDataSourceResponse getDataSource(KendraClient client) {
        DescribeDataSourceResponse index = null;

        try {
            index = client.describeDataSource(r -> r.id(getId()).indexId(getIndex().getId()));

        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return index;
    }

    private Boolean isSyncing(KendraClient client) {
        boolean isSyncing = false;

        try {
            ListDataSourceSyncJobsResponse syncJobs = client.listDataSourceSyncJobs(r -> r.id(
                getId()).indexId(getIndex().getId()));

            if (syncJobs.hasHistory()) {
                for (DataSourceSyncJob j : syncJobs.history()) {
                    if (j.status().equals(DataSourceSyncJobStatus.SYNCING) || j.status()
                        .equals(DataSourceSyncJobStatus.SYNCING_INDEXING)) {
                        isSyncing = true;
                        break;
                    }
                }
            }
        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return isSyncing;
    }

    private String getArn() {
        return String.format(
            "arn:aws:kendra:%s:%s:index/%s/data-source/%s",
            credentials(AwsCredentials.class).getRegion(),
            getRole().getArn().split(":")[4],
            getIndex().getId(),
            getId());
    }
}
