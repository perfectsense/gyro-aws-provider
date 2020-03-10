/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;
import software.amazon.awssdk.services.elasticsearch.model.CreateElasticsearchDomainRequest;
import software.amazon.awssdk.services.elasticsearch.model.CreateElasticsearchDomainResponse;
import software.amazon.awssdk.services.elasticsearch.model.DescribeElasticsearchDomainResponse;
import software.amazon.awssdk.services.elasticsearch.model.ElasticsearchDomainStatus;
import software.amazon.awssdk.services.elasticsearch.model.ElasticsearchException;
import software.amazon.awssdk.services.elasticsearch.model.Tag;
import software.amazon.awssdk.services.elasticsearch.model.UpdateElasticsearchDomainConfigRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates an elasticsearch domain.
 *
 * Example
 * -------
 * .. code-block:: gyro
 *
 *      aws::elasticsearch-domain elasticsearch-domain-example
 *          domain-name: "testdomain"
 *          elastic-search-version: "7.1"
 *
 *          ebs-options
 *              enable-ebs: true
 *              volume-type: standard
 *              volume-count: 10
 *          end
 *
 *          node-to-node-encryption-options
 *              enable-node-to-node-encryption: true
 *          end
 *
 *          encryption-at-rest-options
 *              enable-encryption-at-rest: true
 *          end
 *
 *          cluster-configuration
 *              enable-zone-awareness: true
 *              instance-count: 4
 *
 *              zone-awareness-configuration
 *                  availability-zone-count: 2
 *              end
 *          end
 *
 *          domain-endpoint-options
 *              enforce-https: true
 *          end
 *
 *          advanced-security-options
 *              enable-advanced-security-options: true
 *              enable-internal-user-database: true
 *
 *              master-user-options
 *                  master-username: "masteruser"
 *                  master-password: "MasterUser1!"
 *              end
 *          end
 *
 *          access-policies: "access-policy.json"
 *
 *          advanced-options: {
 *              "indices.query.bool.max_clause_count": "1026"
 *          }
 *
 *          tags: {
 *              "description": "Test Domain"
 *          }
 *
 *          vpc-options
 *              subnets: [
 *                  $(aws::subnet example-subnet-1),
 *                  $(aws::subnet example-subnet-3)
 *              ]
 *
 *              security-groups: [
 *                  $(aws::security-group example-security-group)
 *              ]
 *          end
 *      end
 */
@Type("elasticsearch-domain")
public class ElasticsearchDomainResource extends AwsResource implements Copyable<ElasticsearchDomainStatus> {

    private String elasticSearchVersion;
    private String domainName;
    private ElasticsearchEbsOptions ebsOptions;
    private ElasticsearchClusterConfiguration clusterConfiguration;
    private ElasticsearchSnapshotOptions snapshotOptions;
    private String accessPolicies;
    private Map<String, String> advancedOptions;
    private ElasticsearchNodeToNodeEncryptionOptions nodeToNodeEncryptionOptions;
    private ElasticsearchDomainEndpointOptions domainEndpointOptions;
    private ElasticsearchEncryptionAtRestOptions encryptionAtRestOptions;
    private ElasticsearchVpcOptions vpcOptions;
    private ElasticsearchAdvancedSecurityOptions advancedSecurityOptions;
    private Map<String, String> tags;

    // Output
    private String id;
    private String arn;

    /**
     * The version of ElasticSearch. Defaults to ``1.5``.
     */
    public String getElasticSearchVersion() {
        return elasticSearchVersion;
    }

    public void setElasticSearchVersion(String elasticSearchVersion) {
        this.elasticSearchVersion = elasticSearchVersion;
    }

    /**
     * The name of the Elasticsearch Domain. (Required)
     */
    @Required
    @Regex("^[a-z]([a-z]|[0-9]|-){2,27}$")
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Options for the Elastic Block Storage.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchEbsOptions
     */
    public ElasticsearchEbsOptions getEbsOptions() {
        return ebsOptions;
    }

    @Updatable
    public void setEbsOptions(ElasticsearchEbsOptions ebsOptions) {
        this.ebsOptions = ebsOptions;
    }

    /**
     * The configuration option for the Elasticsearch Domain cluster.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchClusterConfiguration
     */
    @Updatable
    public ElasticsearchClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public void setClusterConfiguration(ElasticsearchClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
    }

    /**
     * Options to set the automated snapshot time.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchSnapshotOptions
     */
    @Updatable
    public ElasticsearchSnapshotOptions getSnapshotOptions() {
        return snapshotOptions;
    }

    public void setSnapshotOptions(ElasticsearchSnapshotOptions snapshotOptions) {
        this.snapshotOptions = snapshotOptions;
    }

    /**
     * The Json formatted IAM access policies.
     */
    @Updatable
    public String getAccessPolicies() {
        if (accessPolicies != null && accessPolicies.contains(".json")) {
            try (InputStream input = openInput(accessPolicies)) {
                accessPolicies = formatPolicy(IoUtils.toUtf8String(input));
                return accessPolicies;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return accessPolicies;
        }
    }

    public void setAccessPolicies(String accessPolicies) {
        this.accessPolicies = accessPolicies;
    }

    /**
     * Options to allow references to indices in an HTTP request body.
     */
    @Updatable
    public Map<String, String> getAdvancedOptions() {
        return advancedOptions;
    }

    public void setAdvancedOptions(Map<String, String> advancedOptions) {
        this.advancedOptions = advancedOptions;
    }

    /**
     * Option to enable node to node encryption for the Elasticsearch domain.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchNodeToNodeEncryptionOptions
     */
    public ElasticsearchNodeToNodeEncryptionOptions getNodeToNodeEncryptionOptions() {
        return nodeToNodeEncryptionOptions;
    }

    public void setNodeToNodeEncryptionOptions(ElasticsearchNodeToNodeEncryptionOptions nodeToNodeEncryptionOptions) {
        this.nodeToNodeEncryptionOptions = nodeToNodeEncryptionOptions;
    }

    /**
     * Options to specify configuration for the Elasticsearch domain endpoint.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchDomainEndpointOptions
     */
    @Updatable
    public ElasticsearchDomainEndpointOptions getDomainEndpointOptions() {
        return domainEndpointOptions;
    }

    public void setDomainEndpointOptions(ElasticsearchDomainEndpointOptions domainEndpointOptions) {
        this.domainEndpointOptions = domainEndpointOptions;
    }

    /**
     * Options to enable encryption at rest and specify the KMS encryption key.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchEncryptionAtRestOptions
     */
    public ElasticsearchEncryptionAtRestOptions getEncryptionAtRestOptions() {
        return encryptionAtRestOptions;
    }

    public void setEncryptionAtRestOptions(ElasticsearchEncryptionAtRestOptions encryptionAtRestOptions) {
        this.encryptionAtRestOptions = encryptionAtRestOptions;
    }

    /**
     * Options to specify the subnets and security groups for VPC endpoint.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchVpcOptions
     */
    @Updatable
    public ElasticsearchVpcOptions getVpcOptions() {
        return vpcOptions;
    }

    public void setVpcOptions(ElasticsearchVpcOptions vpcOptions) {
        this.vpcOptions = vpcOptions;
    }

    /**
     * Specify the advanced security options.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchAdvancedSecurityOptions
     */
    @Updatable
    public ElasticsearchAdvancedSecurityOptions getAdvancedSecurityOptions() {
        return advancedSecurityOptions;
    }

    public void setAdvancedSecurityOptions(ElasticsearchAdvancedSecurityOptions advancedSecurityOptions) {
        this.advancedSecurityOptions = advancedSecurityOptions;
    }

    /**
     * The list of tags.
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
     * The ID of the Elasticsearch domain.
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
     * The Amazon Resource Name of an Elasticsearch domain.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(ElasticsearchDomainStatus model) {
        copyFrom(model, true);
    }

    @Override
    public boolean refresh() {
        ElasticsearchClient client = createClient(ElasticsearchClient.class);

        ElasticsearchDomainStatus domain = getElasticSearchDomain(client);

        if (domain == null) {
            return false;
        }

        ElasticsearchAdvancedSecurityOptions advancedSecurityOptions = getAdvancedSecurityOptions();

        copyFrom(domain);

        if (advancedSecurityOptions != null && advancedSecurityOptions.getMasterUserOptions() != null) {
            getAdvancedSecurityOptions().setMasterUserOptions(advancedSecurityOptions.getMasterUserOptions());
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ElasticsearchClient client = createClient(ElasticsearchClient.class);

        CreateElasticsearchDomainRequest.Builder builder = CreateElasticsearchDomainRequest.builder()
            .domainName(getDomainName())
            .elasticsearchVersion(getElasticSearchVersion())
            .accessPolicies(getAccessPolicies())
            .nodeToNodeEncryptionOptions(getNodeToNodeEncryptionOptions().toNodeEncryptionOptions())
            .advancedOptions(getAdvancedOptions());

        if (getEbsOptions() != null) {
            builder = builder.ebsOptions(getEbsOptions().toEBSOptions());
        }

        if (getClusterConfiguration() != null) {
            builder = builder.elasticsearchClusterConfig(getClusterConfiguration().toElasticSearchClusterConfig());
        }

        if (getSnapshotOptions() != null) {
            builder = builder.snapshotOptions(getSnapshotOptions().toSnapshotOptions());
        }

        if (getEncryptionAtRestOptions() != null) {
            builder = builder.encryptionAtRestOptions(getEncryptionAtRestOptions().toEncryptionAtRestOptions());
        }

        if (getAdvancedSecurityOptions() != null) {
            builder = builder.advancedSecurityOptions(getAdvancedSecurityOptions().toAdvancedSecurityOptionsInput());
        }

        if (getVpcOptions() != null) {
            builder = builder.vpcOptions(getVpcOptions().toVPCOptions());
        }

        if (getDomainEndpointOptions() != null) {
            builder = builder.domainEndpointOptions(getDomainEndpointOptions().toDomainEndpointOptions());
        }

        CreateElasticsearchDomainResponse response = client.createElasticsearchDomain(builder.build());

        // Storing and resetting the latest MasterUserOptions here since the api doesn't return the master username and password.
        ElasticsearchMasterUserOptions masterUserOptions = getAdvancedSecurityOptions().getMasterUserOptions();

        copyFrom(response.domainStatus(), false);

        if (masterUserOptions != null) {
            getAdvancedSecurityOptions().setMasterUserOptions(masterUserOptions);
        }

        addTags(client);

        waitForAvailability(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ElasticsearchClient client = createClient(ElasticsearchClient.class);
        ElasticsearchDomainResource currentResource = (ElasticsearchDomainResource) current;

        UpdateElasticsearchDomainConfigRequest.Builder builder = UpdateElasticsearchDomainConfigRequest.builder()
            .domainName(getDomainName());

        if (changedFieldNames.contains("ebs-options")) {
            builder = builder.ebsOptions(getEbsOptions().toEBSOptions());
        }

        if (changedFieldNames.contains("access-policies")) {
            builder = builder.accessPolicies(getAccessPolicies());
        }

        if (changedFieldNames.contains("advanced-options")) {
            builder = builder.advancedOptions(getAdvancedOptions());
        }

        if (changedFieldNames.contains("cluster-configuration")) {
            builder = builder.elasticsearchClusterConfig(getClusterConfiguration().toElasticSearchClusterConfig());
        }

        if (changedFieldNames.contains("snapshot-options")) {
            builder = builder.snapshotOptions(getSnapshotOptions().toSnapshotOptions());
        }

        if (changedFieldNames.contains("advanced-security-options")) {
            builder = builder.advancedSecurityOptions(getAdvancedSecurityOptions().toAdvancedSecurityOptionsInput());
        }

        if (changedFieldNames.contains("domain-endpoint-options")) {
            builder = builder.domainEndpointOptions(getDomainEndpointOptions().toDomainEndpointOptions());
        }

        if (changedFieldNames.contains("vpc-options")) {
            builder = builder.vpcOptions(getVpcOptions().toVPCOptions());
        }

        client.updateElasticsearchDomainConfig(builder.build());

        if (changedFieldNames.contains("tags")) {
            if (!currentResource.getTags().isEmpty()) {
                client.removeTags(r -> r.arn(currentResource.getArn())
                    .tagKeys(new ArrayList<String>(currentResource.getTags().keySet())));
            }

            addTags(client);
        }

        waitForAvailability(client);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ElasticsearchClient client = createClient(ElasticsearchClient.class);

        client.deleteElasticsearchDomain(r -> r.domainName(getDomainName()));

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(4, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getElasticSearchDomain(client) == null);
    }

    public String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ")
            .replaceAll("\t", " ")
            .trim()
            .replaceAll(" ", "") : policy;
    }

    private ElasticsearchDomainStatus getElasticSearchDomain(ElasticsearchClient client) {
        ElasticsearchDomainStatus domain = null;

        try {
            DescribeElasticsearchDomainResponse response = client.describeElasticsearchDomain(r -> r.domainName(
                getDomainName()));

            if (response != null && response.domainStatus() != null && (response.domainStatus()
                .deleted()
                .equals(Boolean.FALSE) || (response.domainStatus()
                .deleted()
                .equals(Boolean.TRUE) && response.domainStatus().processing().equals(Boolean.TRUE)))
            ) {
                domain = response.domainStatus();
            }

        } catch (ElasticsearchException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("ResourceNotFoundException")) {
                throw ex;
            }
        }

        return domain;
    }

    private void copyFrom(ElasticsearchDomainStatus model, boolean refreshTags) {
        setId(model.domainId());
        setAccessPolicies(model.accessPolicies());
        setAdvancedOptions(model.advancedOptions());
        setDomainName(model.domainName());
        setElasticSearchVersion(model.elasticsearchVersion());
        setArn(model.arn());

        if (model.ebsOptions() != null) {
            ElasticsearchEbsOptions elasticsearchEbsOptions = newSubresource(ElasticsearchEbsOptions.class);
            elasticsearchEbsOptions.copyFrom(model.ebsOptions());
            setEbsOptions(elasticsearchEbsOptions);
        }

        if (model.elasticsearchClusterConfig() != null) {
            ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = newSubresource(
                ElasticsearchClusterConfiguration.class);
            elasticsearchClusterConfiguration.copyFrom(model.elasticsearchClusterConfig());
            setClusterConfiguration(elasticsearchClusterConfiguration);
        }

        if (model.snapshotOptions() != null) {
            ElasticsearchSnapshotOptions elasticsearchSnapshotOptions = newSubresource(ElasticsearchSnapshotOptions.class);
            elasticsearchSnapshotOptions.copyFrom(model.snapshotOptions());
            setSnapshotOptions(elasticsearchSnapshotOptions);
        }

        if (model.nodeToNodeEncryptionOptions() != null) {
            ElasticsearchNodeToNodeEncryptionOptions elasticsearchNodeToNodeEncryptionOptions = newSubresource(
                ElasticsearchNodeToNodeEncryptionOptions.class);
            elasticsearchNodeToNodeEncryptionOptions.copyFrom(model.nodeToNodeEncryptionOptions());
            setNodeToNodeEncryptionOptions(elasticsearchNodeToNodeEncryptionOptions);
        }

        if (model.domainEndpointOptions() != null) {
            ElasticsearchDomainEndpointOptions elasticsearchDomainEndpointOptions = newSubresource(
                ElasticsearchDomainEndpointOptions.class);
            elasticsearchDomainEndpointOptions.copyFrom(model.domainEndpointOptions());
            setDomainEndpointOptions(elasticsearchDomainEndpointOptions);
        }

        if (model.encryptionAtRestOptions() != null) {
            ElasticsearchEncryptionAtRestOptions elasticsearchEncryptionAtRestOptions = newSubresource(
                ElasticsearchEncryptionAtRestOptions.class);
            elasticsearchEncryptionAtRestOptions.copyFrom(model.encryptionAtRestOptions());
            setEncryptionAtRestOptions(elasticsearchEncryptionAtRestOptions);
        }

        if (model.vpcOptions() != null) {
            ElasticsearchVpcOptions elasticsearchVpcOptions = newSubresource(ElasticsearchVpcOptions.class);
            elasticsearchVpcOptions.copyFrom(model.vpcOptions());
            setVpcOptions(elasticsearchVpcOptions);
        }

        if (model.advancedSecurityOptions() != null) {
            ElasticsearchAdvancedSecurityOptions elasticsearchAdvancedSecurityOptions = newSubresource(
                ElasticsearchAdvancedSecurityOptions.class);
            elasticsearchAdvancedSecurityOptions.copyFrom(model.advancedSecurityOptions());
            setAdvancedSecurityOptions(elasticsearchAdvancedSecurityOptions);
        }

        if (refreshTags) {
            ElasticsearchClient client = createClient(ElasticsearchClient.class);

            getTags().clear();

            client.listTags(r -> r.arn(getArn())).tagList().forEach(t -> getTags().put(t.key(), t.value()));
        }
    }

    private void addTags(ElasticsearchClient client) {
        if (!getTags().isEmpty()) {
            client.addTags(r -> r.arn(getArn())
                .tagList(getTags().entrySet()
                    .stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toList())));
        }
    }

    private void waitForAvailability(ElasticsearchClient client) {
        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(4, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> {
                ElasticsearchDomainStatus elasticSearchDomain = getElasticSearchDomain(client);
                return elasticSearchDomain != null && elasticSearchDomain.processing().equals(Boolean.FALSE)
                    && elasticSearchDomain.created().equals(Boolean.TRUE);
            });
    }
}
