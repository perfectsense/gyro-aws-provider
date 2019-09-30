package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.diff.Delete;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
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

    /**
     *  ARN of the certificate. (Required)
     */
    @Updatable
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  Determines if the certificate is default. (Optional)
     */
    @Updatable
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
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        client.addListenerCertificates(r -> r.certificates(toCertificate())
                                            .listenerArn(getListenerArn()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        if (DiffableInternals.hasChange(parentResource(), Delete.class)) {
            return;
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.removeListenerCertificates(r -> r.certificates(toCertificate())
                .listenerArn(getListenerArn()));
    }

    private Certificate toCertificate() {
        return Certificate.builder()
                .certificateArn(getArn())
                .build();
    }
}
