/*
 * Copyright 2025, Brightspot.
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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.CreateOutboundConnectionResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeOutboundConnectionsResponse;
import software.amazon.awssdk.services.opensearch.model.Filter;
import software.amazon.awssdk.services.opensearch.model.OutboundConnection;
import software.amazon.awssdk.services.opensearch.model.OutboundConnectionStatusCode;

/**
 * Create an OpenSearch Outbound connection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-outbound-connection open-search-outbound-example
 *         connection-alias: "outbound-example"
 *         local-domain-name: $(aws::opensearch-domain "open-search-domain-example-1")
 *         remote-domain-name: $(aws::opensearch-domain "open-search-domain-example-2")
 *         connection-properties
 *             cross-cluster-search
 *                 skip-unavailable-clusters: ENABLED
 *             end
 *         end
 *     end
 */
@Type("opensearch-outbound-connection")
public class OpenSearchOutboundConnectionResource extends AwsResource implements Copyable<OutboundConnection> {
    private String connectionAlias;
    private String connectionMode;
    private OpenSearchConnectionProperties connectionProperties;
    private OpenSearchDomainInformationContainer localDomainInfo;
    private OpenSearchDomainInformationContainer remoteDomainInfo;

    // Read-only
    private String connectionId;
    private String connectionStatus;

    /**
     * The name of the connection
     */
    @Required
    @Regex("[a-zA-Z][a-zA-Z0-9\\-\\_]+")
    public String getConnectionAlias() {
        return connectionAlias;
    }

    public void setConnectionAlias(String connectionAlias) {
        this.connectionAlias = connectionAlias;
    }

    /**
     * The connection mode. Defaults to ``DIRECT``
     */
    @ValidStrings({ "DIRECT", "VPC_ENDPOINT" })
    public String getConnectionMode() {
        if (connectionMode == null) {
            connectionMode = "DIRECT";
        }
        return connectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        this.connectionMode = connectionMode;
    }

    /**
     * The information for the local (source) OpenSearch domain.
     */
    @Required
    public OpenSearchDomainInformationContainer getLocalDomainInfo() {
        return localDomainInfo;
    }

    public void setLocalDomainInfo(OpenSearchDomainInformationContainer localDomainInfo) {
        this.localDomainInfo = localDomainInfo;
    }

    /**
     * The information for the remote (destination) OpenSearch domain.
     */
    @Required
    public OpenSearchDomainInformationContainer getRemoteDomainInfo() {
        return remoteDomainInfo;
    }

    public void setRemoteDomainInfo(OpenSearchDomainInformationContainer remoteDomainInfo) {
        this.remoteDomainInfo = remoteDomainInfo;
    }


    /**
     * The connection properties for the OpenSearch connection.
     */
    public OpenSearchConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(OpenSearchConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * The connection id for the resource
     */
    @Id
    @Output
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * The status of the connection.
     */
    @Output
    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    @Override
    public void copyFrom(OutboundConnection model) {
        setConnectionAlias(model.connectionAlias());
        setConnectionId(model.connectionId());
        setConnectionMode(String.valueOf(model.connectionMode()));
        setConnectionStatus(String.valueOf(model.connectionStatus()));

        setConnectionProperties(null);
        if (model.connectionProperties() != null) {
            OpenSearchConnectionProperties newConnectionProperties =
                newSubresource(OpenSearchConnectionProperties.class);
            newConnectionProperties.copyFrom(model.connectionProperties());
            setConnectionProperties(newConnectionProperties);
        }

        setLocalDomainInfo(null);
        if (model.localDomainInfo() != null) {
            OpenSearchDomainInformationContainer localInformationContainer =
                newSubresource(OpenSearchDomainInformationContainer.class);
            localInformationContainer.copyFrom(model.localDomainInfo());
            setLocalDomainInfo(localInformationContainer);
        }

        setRemoteDomainInfo(null);
        if (model.remoteDomainInfo() != null) {
            OpenSearchDomainInformationContainer remoteInformationContainer =
                newSubresource(OpenSearchDomainInformationContainer.class);
            remoteInformationContainer.copyFrom(model.remoteDomainInfo());
            setRemoteDomainInfo(remoteInformationContainer);
        }
    }

    @Override
    public boolean refresh() {
        OpenSearchClient client = createClient(OpenSearchClient.class);

        DescribeOutboundConnectionsResponse response = client.describeOutboundConnections(x -> x.filters(
            Filter.builder()
                .name("connection-id")
                .values(getConnectionId())
                .build()
        ));

        if (response.hasConnections() && !response.connections().isEmpty()) {
            OutboundConnection connection = response.connections().get(0);

            copyFrom(connection);

            return true;

        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        OpenSearchClient client = createClient(OpenSearchClient.class);

        // Requester
        CreateOutboundConnectionResponse response = client.createOutboundConnection(
            r -> r.connectionAlias(getConnectionAlias())
                .connectionMode(getConnectionMode())
                .localDomainInfo(getLocalDomainInfo().toDomainInformationContainer())
                .remoteDomainInfo(getRemoteDomainInfo().toDomainInformationContainer())
                .connectionProperties(
                    getConnectionProperties() != null ? getConnectionProperties().toConnectionProperties() : null)
        );

        setConnectionId(response.connectionId());
        state.save();

        String uiMessage = "\n@|bold,white   · Waiting for '%s' Status... |@";
        ui.write(String.format(uiMessage, OutboundConnectionStatusCode.PENDING_ACCEPTANCE));
        waitForStatus(client, OutboundConnectionStatusCode.PENDING_ACCEPTANCE);

        // Accepter
        Optional.ofNullable(getRemoteDomainInfo().getAwsDomainInformation())
            .ifPresent(r -> r.getDomain().acceptInboundConnection(response.connectionId()));

        ui.write(String.format(uiMessage, OutboundConnectionStatusCode.ACTIVE));
        waitForStatus(client, OutboundConnectionStatusCode.ACTIVE);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        throw new GyroException("Update not supported for OpenSearch Outbound Connection");
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        OpenSearchClient client = createClient(OpenSearchClient.class);
        client.deleteOutboundConnection(r -> r.connectionId(getConnectionId()));
        waitForStatus(client, OutboundConnectionStatusCode.DELETED);
    }

    private void waitForStatus(OpenSearchClient client, OutboundConnectionStatusCode status) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(15, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> {
                DescribeOutboundConnectionsResponse response = client.describeOutboundConnections(x -> x.filters(
                    Filter.builder()
                        .name("connection-id")
                        .values(getConnectionId())
                        .build()
                ));

                OutboundConnection connection = null;
                for (OutboundConnection outboundConnection : response.connections()) {
                    if (outboundConnection.connectionId().equals(getConnectionId())) {
                        connection = outboundConnection;
                        break;
                    }
                }

                if (connection == null || connection.connectionStatus() == null) {
                    return false;
                } else {
                    return status.equals(connection.connectionStatus().statusCode());
                }
            });
    }
}
