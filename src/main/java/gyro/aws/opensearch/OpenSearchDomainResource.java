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

package gyro.aws.opensearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.CreateDomainRequest;
import software.amazon.awssdk.services.opensearch.model.CreateDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainConfigResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DomainConfig;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;
import software.amazon.awssdk.services.opensearch.model.IPAddressType;
import software.amazon.awssdk.services.opensearch.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearch.model.Tag;
import software.amazon.awssdk.services.opensearch.model.UpdateDomainConfigRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates an opensearch domain.
 *
 * Example
 * -------
 * .. code-block:: gyro
 *
 *     aws::opensearch-domain opensearch-domain-example
 *         domain-name: "testdomain3"
 *         open-search-version: "OpenSearch_1.0"
 *
 *         ebs-options
 *             enable-ebs: true
 *             volume-type: standard
 *             volume-count: 10
 *         end
 *
 *         node-to-node-encryption-options
 *             enable-node-to-node-encryption: true
 *         end
 *
 *         encryption-at-rest-options
 *             enable-encryption-at-rest: true
 *         end
 *
 *         cluster-configuration
 *             enable-zone-awareness: true
 *             instance-count: 4
 *
 *             zone-awareness-configuration
 *                 availability-zone-count: 2
 *             end
 *         end
 *
 *         domain-endpoint-options
 *             enforce-https: true
 *         end
 *
 *         advanced-security-options
 *             enable-advanced-security-options: true
 *             enable-internal-user-database: true
 *
 *             master-user-options
 *                 master-username: "masteruser"
 *                 master-password: "MasterUser1!"
 *             end
 *         end
 *
 *         access-policies: "access-policy.json"
 *
 *         advanced-options: {
 *             "indices.query.bool.max_clause_count": "1026"
 *         }
 *
 *         tags: {
 *             "description": "Test Domain"
 *         }
 *
 *         vpc-options
 *             subnets: [
 *                 $(aws::subnet example-subnet-1),
 *                 $(aws::subnet example-subnet-3)
 *             ]
 *
 *             security-groups: [
 *                 $(aws::security-group example-security-group)
 *             ]
 *         end
 *     end
 */
@Type("opensearch-domain")
public class OpenSearchDomainResource extends AwsResource implements Copyable<DomainStatus> {

    private String openSearchVersion;
    private String domainName;
    private OpenSearchEbsOptions ebsOptions;
    private OpenSearchClusterConfiguration clusterConfiguration;
    private OpenSearchSnapshotOptions snapshotOptions;
    private String accessPolicies;
    private Map<String, String> advancedOptions;
    private OpenSearchNodeToNodeEncryptionOptions nodeToNodeEncryptionOptions;
    private OpenSearchDomainEndpointOptions domainEndpointOptions;
    private OpenSearchEncryptionAtRestOptions encryptionAtRestOptions;
    private OpenSearchVpcOptions vpcOptions;
    private OpenSearchAdvancedSecurityOptions advancedSecurityOptions;
    private OpenSearchOffPeakWindowOptions offPeakWindowOptions;
    private OpenSearchAutoTuneOptions autoTuneOptions;
    private IPAddressType ipAddressType;
    private Map<String, String> tags;

    // Output
    private String id;
    private String arn;
    private Map<String, String> endpoints;
    private String endpoint;
    private String endpointV2;

    /**
     * The version of OpenSearch.
     */
    @Regex(value = "Elasticsearch_\\d+\\.\\d+", message = "Should be in the format of 'Elasticsearch_X.Y'")
    @Regex(value = "OpenSearch_\\d+\\.\\d+", message = "Should be in the format of 'OpenSearch_X.Y'")
    public String getOpenSearchVersion() {
        return openSearchVersion;
    }

    public void setOpenSearchVersion(String elasticSearchVersion) {
        this.openSearchVersion = elasticSearchVersion;
    }

    /**
     * The name of the OpenSearch Domain. The name can be a combination of lowercase letters, numbers, or hyphens (``-``) and it must start with a lowercase letter. It can be between ``3`` to ``28`` characters in length.
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
     * The Elastic Block Storage options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchEbsOptions
     */
    @Updatable
    public OpenSearchEbsOptions getEbsOptions() {
        return ebsOptions;
    }

    public void setEbsOptions(OpenSearchEbsOptions ebsOptions) {
        this.ebsOptions = ebsOptions;
    }

    /**
     * The OpenSearch Domain cluster configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchClusterConfiguration
     */
    @Updatable
    public OpenSearchClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public void setClusterConfiguration(OpenSearchClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
    }

    /**
     * The automated snapshot time configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchSnapshotOptions
     */
    @Updatable
    public OpenSearchSnapshotOptions getSnapshotOptions() {
        return snapshotOptions;
    }

    public void setSnapshotOptions(OpenSearchSnapshotOptions snapshotOptions) {
        this.snapshotOptions = snapshotOptions;
    }

    /**
     * The Json formatted IAM access policies. It can either be a JSON formatted string or the file path to a ``.json`` file.
     */
    @Updatable
    public String getAccessPolicies() {
        if (accessPolicies != null && accessPolicies.contains(".json")) {
            try (InputStream input = openInput(accessPolicies)) {
                accessPolicies = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return accessPolicies;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(accessPolicies);
        }
    }

    public void setAccessPolicies(String accessPolicies) {
        this.accessPolicies = accessPolicies;
    }

    /**
     * Configure advanced options for the cluster to allow references to indices in an HTTP request body. The valid options are
     * ``rest.action.multi.allow_explicit_index``: ``true`` | ``false``
     * ``override_main_response_version``: ``true`` | ``false``
     * ``indices.fielddata.cache.size``: A number between ``1`` and ``100``
     * ``indices.query.bool.max_clause``: A number between ``1`` and ``2147483647``.
     */
    @Updatable
    public Map<String, String> getAdvancedOptions() {
        if (advancedOptions == null) {
            advancedOptions = new HashMap<>();
        }

        return advancedOptions;
    }

    public void setAdvancedOptions(Map<String, String> advancedOptions) {
        this.advancedOptions = advancedOptions;
    }

    /**
     * The node to node encryption options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchNodeToNodeEncryptionOptions
     */
    public OpenSearchNodeToNodeEncryptionOptions getNodeToNodeEncryptionOptions() {
        return nodeToNodeEncryptionOptions;
    }

    public void setNodeToNodeEncryptionOptions(OpenSearchNodeToNodeEncryptionOptions nodeToNodeEncryptionOptions) {
        this.nodeToNodeEncryptionOptions = nodeToNodeEncryptionOptions;
    }

    /**
     * The OpenSearch domain endpoint options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchDomainEndpointOptions
     */
    @Updatable
    public OpenSearchDomainEndpointOptions getDomainEndpointOptions() {
        return domainEndpointOptions;
    }

    public void setDomainEndpointOptions(OpenSearchDomainEndpointOptions domainEndpointOptions) {
        this.domainEndpointOptions = domainEndpointOptions;
    }

    /**
     * The encryption at rest options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchEncryptionAtRestOptions
     */
    @Updatable
    public OpenSearchEncryptionAtRestOptions getEncryptionAtRestOptions() {
        return encryptionAtRestOptions;
    }

    public void setEncryptionAtRestOptions(OpenSearchEncryptionAtRestOptions encryptionAtRestOptions) {
        this.encryptionAtRestOptions = encryptionAtRestOptions;
    }

    /**
     * The VPC options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchVpcOptions
     */
    @Updatable
    public OpenSearchVpcOptions getVpcOptions() {
        return vpcOptions;
    }

    public void setVpcOptions(OpenSearchVpcOptions vpcOptions) {
        this.vpcOptions = vpcOptions;
    }

    /**
     * The advanced security options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchAdvancedSecurityOptions
     */
    @Updatable
    public OpenSearchAdvancedSecurityOptions getAdvancedSecurityOptions() {
        return advancedSecurityOptions;
    }

    public void setAdvancedSecurityOptions(OpenSearchAdvancedSecurityOptions advancedSecurityOptions) {
        this.advancedSecurityOptions = advancedSecurityOptions;
    }

    /**
     * The off-peak window options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchOffPeakWindowOptions
     */
    @Updatable
    public OpenSearchOffPeakWindowOptions getOffPeakWindowOptions() {
        return offPeakWindowOptions;
    }

    public void setOffPeakWindowOptions(OpenSearchOffPeakWindowOptions offPeakWindowOptions) {
        this.offPeakWindowOptions = offPeakWindowOptions;
    }

    /**
     * The IP address type for the OpenSearch domain.
     */
    @Updatable
    @ValidStrings({ "ipv4", "dualstack" })
    public IPAddressType getIpAddressType() {
        return ipAddressType;
    }

    public void setIpAddressType(IPAddressType ipAddressType) {
        this.ipAddressType = ipAddressType;
    }

    /**
     * The auto-tune options configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchAutoTuneOptions
     */
    @Updatable
    public OpenSearchAutoTuneOptions getAutoTuneOptions() {
        return autoTuneOptions;
    }

    public void setAutoTuneOptions(OpenSearchAutoTuneOptions autoTuneOptions) {
        this.autoTuneOptions = autoTuneOptions;
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
     * The ID of the OpenSearch domain.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The Amazon Resource Name of an OpenSearch domain.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The endpoints of the OpenSearch domain.
     */
    @Output
    public Map<String, String> getEndpoints() {
        if (endpoints == null) {
            endpoints = new HashMap<>();
        }

        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * The Domain-specific endpoint used to submit index, search, and data upload requests to the domain.
     */
    @Output
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The V2 endpoint of the OpenSearch domain.
     * This endpoint functions like a normal endpoint, except that it works with both IPv4 and IPv6 IP addresses.
     * Normal endpoints work only with IPv4 IP addresses.
     * This is provided if the domain is created with the ``ip-address-type`` set to ``dualstack``.
     */
    @Output
    public String getEndpointV2() {
        return endpointV2;
    }

    public void setEndpointV2(String endpointV2) {
        this.endpointV2 = endpointV2;
    }

    @Override
    public void copyFrom(DomainStatus model) {
        setId(model.domainId());
        setAccessPolicies(model.accessPolicies());
        setAdvancedOptions(model.advancedOptions());
        setDomainName(model.domainName());
        setOpenSearchVersion(model.engineVersion());
        setIpAddressType(model.ipAddressType());
        setArn(model.arn());
        setEndpoints(model.endpoints());
        setEndpoint(model.endpoint());
        setEndpointV2(model.endpointV2());

        setEbsOptions(null);
        if (model.ebsOptions() != null) {
            OpenSearchEbsOptions openSearchEbsOptions = newSubresource(OpenSearchEbsOptions.class);
            openSearchEbsOptions.copyFrom(model.ebsOptions());
            setEbsOptions(openSearchEbsOptions);
        }

        setClusterConfiguration(null);
        if (model.clusterConfig() != null) {
            OpenSearchClusterConfiguration openSearchClusterConfiguration = newSubresource(
                OpenSearchClusterConfiguration.class);
            openSearchClusterConfiguration.copyFrom(model.clusterConfig());
            setClusterConfiguration(openSearchClusterConfiguration);
        }

        setSnapshotOptions(null);
        if (model.snapshotOptions() != null) {
            OpenSearchSnapshotOptions openSearchSnapshotOptions = newSubresource(OpenSearchSnapshotOptions.class);
            openSearchSnapshotOptions.copyFrom(model.snapshotOptions());
            setSnapshotOptions(openSearchSnapshotOptions);
        }

        setNodeToNodeEncryptionOptions(null);
        if (model.nodeToNodeEncryptionOptions() != null) {
            OpenSearchNodeToNodeEncryptionOptions openSearchNodeToNodeEncryptionOptions = newSubresource(
                OpenSearchNodeToNodeEncryptionOptions.class);
            openSearchNodeToNodeEncryptionOptions.copyFrom(model.nodeToNodeEncryptionOptions());
            setNodeToNodeEncryptionOptions(openSearchNodeToNodeEncryptionOptions);
        }

        setDomainEndpointOptions(null);
        if (model.domainEndpointOptions() != null) {
            OpenSearchDomainEndpointOptions openSearchDomainEndpointOptions = newSubresource(
                OpenSearchDomainEndpointOptions.class);
            openSearchDomainEndpointOptions.copyFrom(model.domainEndpointOptions());
            setDomainEndpointOptions(openSearchDomainEndpointOptions);
        }

        setEncryptionAtRestOptions(null);
        if (model.encryptionAtRestOptions() != null) {
            OpenSearchEncryptionAtRestOptions openSearchEncryptionAtRestOptions = newSubresource(
                OpenSearchEncryptionAtRestOptions.class);
            openSearchEncryptionAtRestOptions.copyFrom(model.encryptionAtRestOptions());
            setEncryptionAtRestOptions(openSearchEncryptionAtRestOptions);
        }

        setVpcOptions(null);
        if (model.vpcOptions() != null) {
            OpenSearchVpcOptions openSearchVpcOptions = newSubresource(OpenSearchVpcOptions.class);
            openSearchVpcOptions.copyFrom(model.vpcOptions());
            setVpcOptions(openSearchVpcOptions);
        }

        OpenSearchAdvancedSecurityOptions oldOptions = getAdvancedSecurityOptions();
        setAdvancedSecurityOptions(null);
        if (model.advancedSecurityOptions() != null) {
            OpenSearchAdvancedSecurityOptions openSearchAdvancedSecurityOptions = newSubresource(
                OpenSearchAdvancedSecurityOptions.class);
            openSearchAdvancedSecurityOptions.copyFrom(model.advancedSecurityOptions());
            openSearchAdvancedSecurityOptions.setMasterUserOptions(oldOptions != null
                ? oldOptions.getMasterUserOptions()
                : null);
            setAdvancedSecurityOptions(openSearchAdvancedSecurityOptions);
        }

        setOffPeakWindowOptions(null);
        if (model.offPeakWindowOptions() != null) {
            OpenSearchOffPeakWindowOptions openSearchOffPeakWindowOptions = newSubresource(
                OpenSearchOffPeakWindowOptions.class);
            openSearchOffPeakWindowOptions.copyFrom(model.offPeakWindowOptions());
            setOffPeakWindowOptions(openSearchOffPeakWindowOptions);
        }

        OpenSearchClient client = createClient(OpenSearchClient.class);

        setAutoTuneOptions(null);
        DomainConfig openSearchDomainConfig = getOpenSearchDomainConfig(client);
        if (model.autoTuneOptions() != null && openSearchDomainConfig != null &&
            openSearchDomainConfig.autoTuneOptions() != null &&
            openSearchDomainConfig.autoTuneOptions().options() != null) {
            OpenSearchAutoTuneOptions openSearchAutoTuneOptions = newSubresource(OpenSearchAutoTuneOptions.class);
            openSearchAutoTuneOptions.copyFrom(openSearchDomainConfig.autoTuneOptions().options());
            setAutoTuneOptions(openSearchAutoTuneOptions);
        }

        if (model.advancedOptions() != null) {
            setAdvancedOptions(model.advancedOptions());
        }

        getTags().clear();
        client.listTags(r -> r.arn(arn)).tagList()
            .forEach(t -> getTags().put(t.key(), t.value()));
    }

    @Override
    public boolean refresh() {
        OpenSearchClient client = createClient(OpenSearchClient.class);
        DomainStatus domain = getOpenSearchDomain(client);

        if (domain == null) {
            return false;
        }

        copyFrom(domain);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CreateDomainRequest.Builder builder = CreateDomainRequest.builder()
            .domainName(getDomainName())
            .advancedOptions(getAdvancedOptions())
            .accessPolicies(getAccessPolicies())
            .engineVersion(getOpenSearchVersion());

        if (getNodeToNodeEncryptionOptions() != null) {
            builder = builder.nodeToNodeEncryptionOptions(getNodeToNodeEncryptionOptions().toNodeEncryptionOptions());
        }

        if (getEbsOptions() != null) {
            builder = builder.ebsOptions(getEbsOptions().toEBSOptions());
        }

        if (getClusterConfiguration() != null) {
            builder = builder.clusterConfig(getClusterConfiguration().toOpenSearchClusterConfig());
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

        if (getOffPeakWindowOptions() != null) {
            builder = builder.offPeakWindowOptions(getOffPeakWindowOptions().toOffPeakWindowOptions());
        }

        if (getIpAddressType() != null) {
            builder = builder.ipAddressType(getIpAddressType());
        }

        if (getAutoTuneOptions() != null) {
            builder = builder.autoTuneOptions(getAutoTuneOptions().toAutoTuneOptionsInput());
        }

        OpenSearchClient client = createClient(OpenSearchClient.class);
        CreateDomainResponse response = client.createDomain(builder.build());
        DomainStatus domainStatus = response.domainStatus();

        setArn(domainStatus.arn());
        setId(domainStatus.domainId());
        setEndpoints(domainStatus.endpoints());
        setEndpoint(domainStatus.endpoint());
        setEndpointV2(domainStatus.endpointV2());

        addTags(client);

        waitForAvailability(client, TimeoutSettings.Action.CREATE);

        OpenSearchMasterUserOptions masterUserOptions = Optional.ofNullable(getAdvancedSecurityOptions())
            .map(OpenSearchAdvancedSecurityOptions::getMasterUserOptions)
            .orElse(null);

        copyFrom(getOpenSearchDomain(client));

        if (masterUserOptions != null) {
            getAdvancedSecurityOptions().setMasterUserOptions(masterUserOptions);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        UpdateDomainConfigRequest.Builder builder = UpdateDomainConfigRequest.builder()
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
            builder = builder.clusterConfig(getClusterConfiguration().toOpenSearchClusterConfig());
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

        if (changedFieldNames.contains("off-peak-window-options")) {
            builder = builder.offPeakWindowOptions(getOffPeakWindowOptions().toOffPeakWindowOptions());
        }

        if (changedFieldNames.contains("encryption-at-rest-options")) {
            builder = builder.encryptionAtRestOptions(getEncryptionAtRestOptions().toEncryptionAtRestOptions());
        }

        if (changedFieldNames.contains("ip-address-type")) {
            builder = builder.ipAddressType(getIpAddressType());
        }

        if (changedFieldNames.contains("auto-tune-options")) {
            builder = builder.autoTuneOptions(getAutoTuneOptions().toAutoTuneOptions());
        }

        OpenSearchClient client = createClient(OpenSearchClient.class);
        client.updateDomainConfig(builder.build());

        OpenSearchDomainResource currentResource = (OpenSearchDomainResource) current;

        if (changedFieldNames.contains("tags")) {
            if (!currentResource.getTags().isEmpty()) {
                client.removeTags(r -> r.arn(currentResource.getArn())
                    .tagKeys(new ArrayList<>(currentResource.getTags().keySet())));
            }

            addTags(client);
        }

        waitForAvailability(client, TimeoutSettings.Action.UPDATE);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try (OpenSearchClient client = createClient(OpenSearchClient.class)) {
            client.deleteDomain(r -> r.domainName(getDomainName()));

            Wait.atMost(20, TimeUnit.MINUTES)
                .checkEvery(4, TimeUnit.MINUTES)
                .resourceOverrides(this, TimeoutSettings.Action.DELETE)
                .prompt(false)
                .until(() -> getOpenSearchDomain(client) == null);
        }
    }

    private DomainStatus getOpenSearchDomain(OpenSearchClient client) {
        DomainStatus domain = null;

        try {
            DescribeDomainResponse response = client.describeDomain(r -> r.domainName(getDomainName()));

            if (response != null && response.domainStatus() != null
                && (Boolean.FALSE.equals(response.domainStatus().deleted())
                || (Boolean.TRUE.equals(response.domainStatus().deleted())
                && Boolean.TRUE.equals(response.domainStatus().processing())))
            ) {
                domain = response.domainStatus();
            }
        } catch (ResourceNotFoundException ex) {
            // Ignore
        }

        return domain;
    }

    private DomainConfig getOpenSearchDomainConfig(OpenSearchClient client) {
        DomainConfig domain = null;

        try {
            DescribeDomainConfigResponse response = client.describeDomainConfig(r -> r.domainName(getDomainName()));

            if (response != null) {
                domain = response.domainConfig();
            }

        } catch (ResourceNotFoundException ex) {
            // Ignore
        }

        return domain;
    }

    private void addTags(OpenSearchClient client) {
        if (!getTags().isEmpty()) {
            client.addTags(r -> r.arn(getArn()).tagList(getTags().entrySet()
                .stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList())));
        }
    }

    private void waitForAvailability(OpenSearchClient client, TimeoutSettings.Action action) {
        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(4, TimeUnit.MINUTES)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> {
                DomainStatus openSearchDomain = getOpenSearchDomain(client);
                return openSearchDomain != null && Boolean.FALSE.equals(openSearchDomain.processing())
                    && Boolean.TRUE.equals(openSearchDomain.created());
            });
    }
}
