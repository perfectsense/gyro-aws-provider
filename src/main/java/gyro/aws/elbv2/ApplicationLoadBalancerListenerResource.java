package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenerCertificatesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::application-load-balancer-listener listener-example
 *         port: "80"
 *         protocol: "HTTPS"
 *         alb: $(aws::application-load-balancer alb-example)
 *         default-certificate: "arn:aws:acm:us-east-2:acct:certificate/certificate-arn"
 *
 *         default-action
 *             target-group-arn: $(aws::load-balancer-target-group target-group-example | target-group-arn)
 *             type: "forward"
 *         end
 *     end
 */
@Type("application-load-balancer-listener")
public class ApplicationLoadBalancerListenerResource extends ListenerResource implements Copyable<Listener> {

    private ApplicationLoadBalancerResource alb;
    private List<ActionResource> defaultAction;

    /**
     *  The alb that the listener is attached to. (Required)
     **/
    public ApplicationLoadBalancerResource getAlb() {
        return alb;
    }

    public void setAlb(ApplicationLoadBalancerResource alb) {
        this.alb = alb;
    }

    /**
     *  List of default actions associated with the listener. (Required)
     *
     *  @subresource gyro.aws.elbv2.ActionResource
     */
    @Updatable
    public List<ActionResource> getDefaultAction() {
        if (defaultAction == null) {
            defaultAction = new ArrayList<>();
        }

        return defaultAction;
    }

    public void setDefaultAction(List<ActionResource> defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Override
    public void copyFrom(Listener listener) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        setDefaultAction(fromDefaultActions(listener.defaultActions()));
        ApplicationLoadBalancerResource alb = findById(ApplicationLoadBalancerResource.class, listener.loadBalancerArn());
        setAlb(alb);

        getCertificate().clear();
        DescribeListenerCertificatesResponse certResponse = client.describeListenerCertificates(r -> r.listenerArn(listener.listenerArn()));
        if (certResponse != null) {
            for (Certificate certificate : certResponse.certificates()) {
                if (!certificate.isDefault()) {
                    CertificateResource cert = new CertificateResource();
                    cert.setArn(certificate.certificateArn());
                    cert.setIsDefault(certificate.isDefault());
                    getCertificate().add(cert);
                }
            }
        }
    }

    @Override
    public boolean refresh() {
        Listener listener = super.internalRefresh();

        if (listener != null) {

            this.copyFrom(listener);

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        CreateListenerResponse response =
                client.createListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                        .defaultActions(toDefaultActions())
                        .loadBalancerArn(getAlb().getArn())
                        .port(getPort())
                        .protocol(getProtocol())
                        .sslPolicy(getSslPolicy()));

        setArn(response.listeners().get(0).listenerArn());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        if (toCertificates().isEmpty() && getProtocol().equals("HTTP")) {
            client.modifyListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                    .defaultActions(toDefaultActions())
                    .listenerArn(getArn())
                    .port(getPort())
                    .protocol(getProtocol())
                    .sslPolicy(null));
        } else {

            client.modifyListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                    .defaultActions(toDefaultActions())
                    .listenerArn(getArn())
                    .port(getPort())
                    .protocol(getProtocol())
                    .sslPolicy(getSslPolicy()));
        }
    }

    @Override
    public void delete() {
        super.delete();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getArn() != null) {
            sb.append("alb listener " + getArn());
        } else {
            sb.append("alb listener ");
        }
        return sb.toString();
    }

    private List<Action> toDefaultActions() {
        List<Action> defaultAction = new ArrayList<>();

        for (ActionResource resource : getDefaultAction()) {
            defaultAction.add(resource.toAction());
        }

        return defaultAction;
    }

    private List<ActionResource> fromDefaultActions(List<Action> actionList) {
        List<ActionResource> actions = new ArrayList<>();

        for (Action action : actionList) {
            ActionResource actionResource = newSubresource(ActionResource.class);
            actionResource.copyFrom(action);
            actions.add(actionResource);
        }

        return actions;
    }

    public void createDefaultAction(ActionResource defaultAction) {
        if (!getDefaultAction().contains(defaultAction)) {
            getDefaultAction().add(defaultAction);
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyListener(r -> r.certificates(toCertificates())
                .defaultActions(toDefaultActions())
                .listenerArn(getArn())
                .port(getPort())
                .protocol(getProtocol())
                .sslPolicy(getSslPolicy()));

    }

    public void updateDefaultAction() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyListener(r -> r.certificates(toCertificates())
                .defaultActions(toDefaultActions())
                .listenerArn(getArn())
                .port(getPort())
                .protocol(getProtocol())
                .sslPolicy(getSslPolicy()));
    }

    public void deleteDefaultAction(ActionResource defaultAction) {
        if (getDefaultAction().contains(defaultAction)) {
            getDefaultAction().remove(defaultAction);
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyListener(r -> r.certificates(toCertificates())
                .defaultActions(toDefaultActions())
                .listenerArn(getArn())
                .port(getPort())
                .protocol(getProtocol())
                .sslPolicy(getSslPolicy()));
    }
}
