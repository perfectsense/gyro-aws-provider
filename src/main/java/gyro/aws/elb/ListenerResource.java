package gyro.aws.elb;

import gyro.aws.AwsResource;
import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Updatable;

import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.Listener;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     listener
 *        instance-port: "443"
 *        instance-protocol: "HTTPS"
 *        load-balancer-port: "443"
 *        protocol: "HTTPS"
 *     end
 */
public class ListenerResource extends AwsResource {

    private Integer instancePort;
    private String instanceProtocol;
    private Integer loadBalancerPort;
    private String protocol;
    private String sslCertificateId;

    /**
     * The port on which the instance is listening.
     */
    @Updatable
    public Integer getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(Integer instancePort) {
        this.instancePort = instancePort;
    }

    /**
     * The protocol to use for routing traffic to instances : HTTP, HTTPS, TCP, SSL.
     */
    @Updatable
    public String getInstanceProtocol() {
        return instanceProtocol;
    }

    public void setInstanceProtocol(String instanceProtocol) {
        this.instanceProtocol = instanceProtocol;
    }

    /**
     * The port on which the load balancer is listening.
     */
    @Updatable
    public Integer getLoadBalancerPort() {
        return loadBalancerPort;
    }

    public void setLoadBalancerPort(Integer loadBalancerPort) {
        this.loadBalancerPort = loadBalancerPort;
    }

    /**
     * The load balancer transport protocol to use for routing: HTTP, HTTPS, TCP, or SSL.
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The Amazon Resource Name(ARN) of the server certificate.
     */
    @Updatable
    public String getSslCertificateId() {
        return sslCertificateId;
    }

    public void setSslCertificateId(String sslCertificateId) {
        this.sslCertificateId = sslCertificateId;
    }

    public String getLoadBalancer() {
        LoadBalancerResource parent = (LoadBalancerResource) parent();

        if (parent != null) {
            return parent.getLoadBalancerName();
        }

        return null;
    }

    @Override
    public String primaryKey() {
        return String.format("%d", getLoadBalancerPort());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(State state) {
        if (DiffableInternals.getChange(parent()) instanceof Create) {
            return;
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        client.createLoadBalancerListeners(r -> r.listeners(toListener())
            .loadBalancerName(getLoadBalancer()));

    }

    @Override
    public void update(State state, Resource current, Set<String> changedFieldNames) {
        delete(state);
        create(state);
    }

    @Override
    public void delete(State state) {
        if (DiffableInternals.getChange(parent()) instanceof Delete) {
            return;
        }
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        client.deleteLoadBalancerListeners(r -> r.loadBalancerName(getLoadBalancer())
                                                .loadBalancerPorts(getLoadBalancerPort()));
    }

    @Override
    public String toDisplayString() {
        return String.format(
            "load balancer listener %s:%d/%s:%d",
            getProtocol(),
            getLoadBalancerPort(),
            getInstanceProtocol(),
            getInstancePort());
    }

    private Listener toListener() {
        Listener newListener = Listener.builder()
            .instancePort(getInstancePort())
            .instanceProtocol(getInstanceProtocol())
            .loadBalancerPort(getLoadBalancerPort())
            .protocol(getProtocol())
            .sslCertificateId(getSslCertificateId())
            .build();

        return newListener;
    }

}
