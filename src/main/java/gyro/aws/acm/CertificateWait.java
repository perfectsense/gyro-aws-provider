package gyro.aws.acm;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.CertificateStatus;

/**
 * Wait for an ACM certificate to be issued.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::acm-certificate-wait wait-for-certificate-example
 *         certificate: $(aws::acm-certificate "example-certificate")
 *
 *         {@literal @}for record-set -in $(aws::record-set record-set-example-*)
 *             {@literal @}depends-on: $(record-set)
 *         {@literal @}end
 *     end
 */
@Type("acm-certificate-wait")
public class CertificateWait extends AwsResource {

    private AcmCertificateResource certificate;

    @Required
    public AcmCertificateResource getCertificate() {
        return certificate;
    }

    public void setCertificate(AcmCertificateResource certificate) {
        this.certificate = certificate;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        waitForCertificateToBeIssued();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {

    }

    private void waitForCertificateToBeIssued() {
        AcmClient client = createClient(AcmClient.class);

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .until(() -> {
                CertificateDetail certificate = client.describeCertificate(r -> r.certificateArn(getCertificate().getArn()))
                    .certificate();
                return certificate != null && certificate.status().equals(CertificateStatus.ISSUED);
            });
    }
}
