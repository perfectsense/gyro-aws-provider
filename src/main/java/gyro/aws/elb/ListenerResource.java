package gyro.aws.elb;

import gyro.aws.AwsResource;
import gyro.core.resource.Create;
import gyro.core.resource.Delete;
import gyro.core.resource.ResourceUpdatable;

import gyro.core.resource.Resource;
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

    public ListenerResource(){}

    /**
     * The port on which the instance is listening.
     */
    @ResourceUpdatable
    public Integer getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(Integer instancePort) {
        this.instancePort = instancePort;
    }

    /**
     * The protocol to use for routing traffic to instances : HTTP, HTTPS, TCP, SSL.
     */
    @ResourceUpdatable
    public String getInstanceProtocol() {
        return instanceProtocol;
    }

    public void setInstanceProtocol(String instanceProtocol) {
        this.instanceProtocol = instanceProtocol;
    }

    /**
     * The port on which the load balancer is listening.
     */
    @ResourceUpdatable
    public Integer getLoadBalancerPort() {
        return loadBalancerPort;
    }

    public void setLoadBalancerPort(Integer loadBalancerPort) {
        this.loadBalancerPort = loadBalancerPort;
    }

    /**
     * The load balancer transport protocol to use for routing: HTTP, HTTPS, TCP, or SSL.
     */
    @ResourceUpdatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The Amazon Resource Name(ARN) of the server certificate.
     */
    @ResourceUpdatable
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

    public Listener toListener() {
        Listener newListener = Listener.builder()
                    .instancePort(getInstancePort())
                    .instanceProtocol(getInstanceProtocol())
                    .loadBalancerPort(getLoadBalancerPort())
                    .protocol(getProtocol())
                    .sslCertificateId(getSslCertificateId())
                    .build();

        return newListener;
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
    public void create() {
        if (parent().change() instanceof Create) {
            return;
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        client.createLoadBalancerListeners(r -> r.listeners(toListener())
            .loadBalancerName(getLoadBalancer()));

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        delete();
        create();
    }

    @Override
    public void delete() {
        if (parent().change() instanceof Delete) {
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

}
