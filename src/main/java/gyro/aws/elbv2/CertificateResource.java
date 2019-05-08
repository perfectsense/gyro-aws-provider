package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.Delete;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     certificate
 *         arn: "arn:aws:acm:us-east-2:acct-number:certificate/certificatearn"
 *     end
 */
public class CertificateResource extends AwsResource {

    private String arn;
    private Boolean isDefault;

    public CertificateResource() {

    }

    /**
     *  ARN of the certificate (Required)
     */
    @ResourceUpdatable
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @ResourceUpdatable
    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getListenerArn() {
        ListenerResource parent;

        if (parentResource() instanceof ApplicationLoadBalancerListenerResource) {
            parent = (ApplicationLoadBalancerListenerResource) parentResource();
        } else {
            parent = (ApplicationLoadBalancerListenerResource) parentResource();
        }

        if (parent != null) {
            return parent.getArn();
        }

        return null;
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getArn());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        client.addListenerCertificates(r -> r.certificates(toCertificate())
                                            .listenerArn(getListenerArn()));
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {}

    @Override
    public void delete() {
        if (parentResource().change() instanceof Delete) {
            return;
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.removeListenerCertificates(r -> r.certificates(toCertificate())
                .listenerArn(getListenerArn()));
    }

    @Override
    public String toDisplayString() {
        return "certificate " + getArn();
    }

    private Certificate toCertificate() {
        return Certificate.builder()
                .certificateArn(getArn())
                .build();
    }
}
