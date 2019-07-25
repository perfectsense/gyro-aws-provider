package gyro.aws.acmpca;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.acmpca.AcmPcaClient;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query certificate authority.
 *
 * .. code-block:: gyro
 *
 *    certificate-authority: $(external-query aws::certificate-authority {})
 */
@Type("certificate-authority")
public class AcmPcaCerificateAuthorityFinder extends AwsFinder<AcmPcaClient, CertificateAuthority, AcmPcaCertificateAuthority> {
    private String arn;

    /**
     * The arn of a Certificate Authority.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<CertificateAuthority> findAllAws(AcmPcaClient client) {
        return client.listCertificateAuthoritiesPaginator().certificateAuthorities().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CertificateAuthority> findAws(AcmPcaClient client, Map<String, String> filters) {
        return Collections.singletonList(client.describeCertificateAuthority(r -> r.certificateAuthorityArn(filters.get("arn"))).certificateAuthority());
    }
}
