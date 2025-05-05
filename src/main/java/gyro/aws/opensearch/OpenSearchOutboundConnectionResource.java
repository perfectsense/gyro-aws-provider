package gyro.aws.opensearch;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
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
import software.amazon.awssdk.services.opensearch.model.DeleteOutboundConnectionResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeOutboundConnectionsResponse;
import software.amazon.awssdk.services.opensearch.model.Filter;
import software.amazon.awssdk.services.opensearch.model.OutboundConnection;
import software.amazon.awssdk.services.opensearch.model.OutboundConnectionStatusCode;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
 *         skip-unavailable-clusters: ENABLED
 *     end
 */
@Type("opensearch-outbound-connection")
public class OpenSearchOutboundConnectionResource extends AwsResource implements Copyable<OutboundConnection> {
    private String connectionAlias;
    private String connectionMode;
    private String skipUnavailableClusters;

    private OpenSearchDomainResource localDomain;
    private OpenSearchDomainResource remoteDomain;
    private String arn;
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
     * The connection mode ``Defaults to (DIRECT)``
     */
    @ValidStrings({"DIRECT", "VPC_ENDPOINT"})
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
     * The status of the connection.
     */
    @Output
    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * The name of the local domain
     */
    @Required
    public OpenSearchDomainResource getLocalDomain() {
        return localDomain;
    }

    public void setLocalDomain(OpenSearchDomainResource localDomain) {
        this.localDomain = localDomain;
    }

    /**
     * The name of the remote domain
     */
    @Required
    public OpenSearchDomainResource getRemoteDomain() {
        return remoteDomain;
    }

    public void setRemoteDomain(OpenSearchDomainResource remoteDomain) {
        this.remoteDomain = remoteDomain;
    }

    /**
     * The direct connection property to skip unavailable clusters. ``Defaults to 'true'``
     */
    @ValidStrings({"ENABLED", "DISABLED"})
    public String getSkipUnavailableClusters() {
        if (skipUnavailableClusters == null) {
            skipUnavailableClusters = "ENABLED";
        }
        return skipUnavailableClusters;
    }

    public void setSkipUnavailableClusters(String skipUnavailableClusters) {
        this.skipUnavailableClusters = skipUnavailableClusters;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }


    @Override
    public void copyFrom(OutboundConnection model) {
        setConnectionAlias(model.connectionAlias());
        setConnectionId(model.connectionId());
        setConnectionMode(String.valueOf(model.connectionMode()));
        setConnectionStatus(String.valueOf(model.connectionStatus()));
    }

    @Override
    public boolean refresh() {
        OpenSearchClient client = createClient(OpenSearchClient.class);

        DescribeOutboundConnectionsResponse response = client.describeOutboundConnections(x -> x.filters(
            r -> r.name("connection-id").values(getConnectionId())
        ));

        if (response.hasConnections() && !response.connections().isEmpty()) {
            OutboundConnection connection = response.connections().get(0);
            if (!OutboundConnectionStatusCode.ACTIVE.equals(connection.connectionStatus().statusCode())) {
                return false;
            }
            copyFrom(connection);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        OpenSearchClient client = createClient(OpenSearchClient.class);

        // Requester
        CreateOutboundConnectionResponse response = client.createOutboundConnection(
            r -> r.connectionAlias(getConnectionAlias())
                .connectionMode(getConnectionMode())
                .localDomainInfo(s -> s.awsDomainInformation(
                    t -> t.domainName(getLocalDomain().getDomainName())
                        .region(getLocalDomain().getRegion())
                        .ownerId(getLocalDomain().getOwnerId())))
                .remoteDomainInfo(s -> s.awsDomainInformation(
                    t -> t.domainName(getRemoteDomain().getDomainName())
                        .region(getRemoteDomain().getRegion())
                        .ownerId(getRemoteDomain().getOwnerId())))
        );

        setConnectionAlias(response.connectionAlias());
        setConnectionId(response.connectionId());
        setConnectionMode(response.connectionModeAsString());
        setSkipUnavailableClusters(String.valueOf(response.connectionProperties().crossClusterSearch().skipUnavailable()));

        waitForStatus(client, OutboundConnectionStatusCode.PENDING_ACCEPTANCE);

        // Accepter
        getRemoteDomain().acceptInboundConnection(response.connectionId());

        waitForStatus(client, OutboundConnectionStatusCode.ACTIVE);

    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        throw new Exception("Update not supported for OpenSearch Outbound Connection");
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        OpenSearchClient client = createClient(OpenSearchClient.class);
        DeleteOutboundConnectionResponse response = client.deleteOutboundConnection(r -> r.connectionId(getConnectionId()));
        waitForStatus(client, OutboundConnectionStatusCode.DELETED);
    }

    private void waitForStatus(OpenSearchClient client, OutboundConnectionStatusCode status) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> {
                DescribeOutboundConnectionsResponse response = client.describeOutboundConnections(x -> x.filters(
                    Filter.builder()
                        .name("local-domain-info.domain-name")
                        .values(getLocalDomain().getDomainName())
                        .build()
                ));

                OutboundConnection connection = null;
                for (OutboundConnection outboundConnection : response.connections()) {
                    if (outboundConnection.connectionId().equals(getConnectionId())) {
                        connection = outboundConnection;
                        break;
                    }
                }

                if (connection == null) {
                    return false;
                } else {
                    return status.equals(connection.connectionStatus().statusCode());
                }
            });
    }
}
