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
import software.amazon.awssdk.services.opensearch.model.DescribeDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DescribeOutboundConnectionsResponse;
import software.amazon.awssdk.services.opensearch.model.Filter;
import software.amazon.awssdk.services.opensearch.model.OutboundConnection;
import software.amazon.awssdk.services.opensearch.model.OutboundConnectionStatusCode;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create an OpenSearch Outbound connection.
 * <p>
 * Example
 * -------
 * <p>
 * .. code-block:: gyro
 * <p>
 * aws::opensearch-outbound-connection open-search-outbound-example
 * connection-alias: "outbound-example"
 * local-domain-name: $(aws::opensearch-domain "open-search-domain-example-1")
 * remote-domain-name: $(aws::opensearch-domain "open-search-domain-example-2")
 * skip-unavailable-clusters: ENABLED
 * end
 */
@Type("opensearch-outbound-connection")
public class OpenSearchOutboundConnectionResource extends AwsResource implements Copyable<OutboundConnection> {
    private String Id;
    private String arn;
    private String ConnectionAlias;
    private String ConnectionId;
    private String ConnectionMode;
    private String ConnectionStatus;
    private String SkipUnavailableClusters;
    private OpenSearchDomainResource LocalDomain;
    private OpenSearchDomainResource RemoteDomain;
    private String LocalOwnerId;
    private String RemoteOwnerId;

    /**
     * The id of the connection.
     */
    @Id
    @Output
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    /**
     * The name of the connection
     */
    @Required
    @Regex("[a-zA-Z][a-zA-Z0-9\\-\\_]+")
    @Output
    public String getConnectionAlias() {
        return ConnectionAlias;
    }

    public void setConnectionAlias(String connectionAlias) {
        ConnectionAlias = connectionAlias;
    }

    /**
     * The connection id for the resource
     */
    @Output
    public String getConnectionId() {
        return ConnectionId;
    }

    public void setConnectionId(String connectionId) {
        ConnectionId = connectionId;
    }

    /**
     * The connection mode ``Defaults to (DIRECT)``
     */
    @ValidStrings({"DIRECT", "VPC_ENDPOINT"})
    public String getConnectionMode() {
        if (ConnectionMode == null) {
            ConnectionMode = "DIRECT";
        }
        return ConnectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        ConnectionMode = connectionMode;
    }


    /**
     * The status of the connection.
     */
    @Output
    public String getConnectionStatus() {
        return ConnectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        ConnectionStatus = connectionStatus;
    }

    /**
     * The name of the local domain
     */
    @Required
    public OpenSearchDomainResource getLocalDomain() {
        return LocalDomain;
    }

    public void setLocalDomain(OpenSearchDomainResource localDomain) {
        LocalDomain = localDomain;
    }

    /**
     * The name of the remote domain
     */
    @Required
    public OpenSearchDomainResource getRemoteDomain() {
        return RemoteDomain;
    }

    public void setRemoteDomain(OpenSearchDomainResource remoteDomain) {
        RemoteDomain = remoteDomain;
    }

    /**
     * The AWS Account ID of the local domain owner.
     */
    @Regex("[0-9]+")
    public String getLocalOwnerId() {
        return LocalOwnerId;
    }

    public void setLocalOwnerId(String localOwnerId) {
        LocalOwnerId = localOwnerId;
    }

    /**
     * The AWS Account ID of the remote domain owner.
     */
    @Regex("[0-9]+")
    public String getRemoteOwnerId() {
        return RemoteOwnerId;
    }

    public void setRemoteOwnerId(String remoteOwnerId) {
        RemoteOwnerId = remoteOwnerId;
    }

    /**
     * The direct connection property to skip unavailable clusters. ``Defaults to 'true'``
     */
    @ValidStrings({"ENABLED", "DISABLED"})
    public String getSkipUnavailableClusters() {
        if (SkipUnavailableClusters == null) {
            SkipUnavailableClusters = "ENABLED";
        }
        return SkipUnavailableClusters;
    }

    public void setSkipUnavailableClusters(String skipUnavailableClusters) {
        SkipUnavailableClusters = skipUnavailableClusters;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }


    @Override
    public void copyFrom(OutboundConnection model) {
        setId(getId());
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
            if (!connection.connectionStatus().statusCode().toString().equals("ACTIVE")) {
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
        OpenSearchClient accepterClient = createClient(OpenSearchClient.class, getRemoteDomain().getArn().split(":")[3], null);

        DescribeDomainResponse localDomainInfo = client.describeDomain(r -> r.domainName(getLocalDomain().getDomainName()));
        DescribeDomainResponse remoteDomainInfo = accepterClient.describeDomain(r -> r.domainName(getRemoteDomain().getDomainName()));

        String localOwnerId = localDomainInfo.domainStatus().arn().split(":")[4];
        String remoteOwnerId = remoteDomainInfo.domainStatus().arn().split(":")[4];

        // Requester
        CreateOutboundConnectionResponse response = client.createOutboundConnection(
            r -> r.connectionAlias(getConnectionAlias())
                .connectionMode(getConnectionMode())
                .localDomainInfo(s -> s.awsDomainInformation(
                    t -> t.domainName(getLocalDomain().getDomainName())
                        .region(localDomainInfo.domainStatus().arn().split(":")[3])
                        .ownerId(localOwnerId)))
                .remoteDomainInfo(s -> s.awsDomainInformation(
                    t -> t.domainName(getRemoteDomain().getDomainName())
                        .region(remoteDomainInfo.domainStatus().arn().split(":")[3])
                        .ownerId(remoteOwnerId)))
        );

        setConnectionAlias(response.connectionAlias());
        setConnectionId(response.connectionId());
        setConnectionMode(response.connectionModeAsString());
        setLocalOwnerId(response.localDomainInfo().awsDomainInformation().ownerId());
        setRemoteOwnerId(response.remoteDomainInfo().awsDomainInformation().ownerId());
        setSkipUnavailableClusters(String.valueOf(response.connectionProperties().crossClusterSearch().skipUnavailable()));

        waitForStatus(client, "PENDING_ACCEPTANCE");

        // Accepter
        accepterClient.acceptInboundConnection(r -> r.connectionId(response.connectionId()));

        waitForStatus(client, "ACTIVE");

    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        throw new Exception("Update not supported for OpenSearch Outbound Connection");
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        OpenSearchClient client = createClient(OpenSearchClient.class);
        DeleteOutboundConnectionResponse response = client.deleteOutboundConnection(r -> r.connectionId(getConnectionId()));
        waitForStatus(client, "DELETED");
    }

    private void waitForStatus(OpenSearchClient client, String status) {
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
                    return connection.connectionStatus().statusCode().toString().equals(status);
                }
            });
    }
}
