package gyro.aws.acm;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.Filters;
import software.amazon.awssdk.services.acm.model.ListCertificatesRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query acm-certificate.
 *
 * .. code-block:: gyro
 *
 *    acm-certificate: $(external-query aws::acm-certificate { certificate-status: "ISSUED"})
 */
@Type("acm-certificate")
public class AcmCertificateFinder extends AwsFinder<AcmClient, CertificateDetail, AcmCertificateResource> {
    private String certificateStatus;
    private String extendedKeyUsage;
    private String keyAlgorithm;
    private String keyUsage;

    /**
     * Status of the certificate. Valid values are ``PENDING_VALIDATION`` or ``ISSUED`` or ``INACTIVE`` or ``EXPIRED`` or ``VALIDATION_TIMED_OUT`` or ``REVOKED`` or ``FAILED``
     */
    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(String certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    /**
     * The extended key usage of the certificate. Valid values are ``ANY`` or ``CODE_SIGNING`` or ``CUSTOM`` or ``EMAIL_PROTECTION`` or ``IPSEC_END_SYSTEM`` or ``IPSEC_TUNNEL`` or ``IPSEC_USER`` or ``NONE`` or ``OCSP_SIGNING`` or ``TIME_STAMPING`` or ``TLS_WEB_CLIENT_AUTHENTICATION`` or ``TLS_WEB_SERVER_AUTHENTICATION``
     */
    public String getExtendedKeyUsage() {
        return extendedKeyUsage;
    }

    public void setExtendedKeyUsage(String extendedKeyUsage) {
        this.extendedKeyUsage = extendedKeyUsage;
    }

    /**
     * The key algorithm for the certificate. Valid values are ``EC_PRIME256_V1`` or ``EC_SECP384_R1`` or ``EC_SECP521_R1`` or ``RSA_1024`` or ``RSA_2048`` or ``RSA_4096``
     */
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * The key usage of the certificate. Valid values are ``ANY`` or ``CERTIFICATE_SIGNING`` or ``CRL_SIGNING`` or ``CUSTOM`` or ``DATA_ENCIPHERMENT`` or ``DECIPHER_ONLY`` or ``DIGITAL_SIGNATURE`` or ``ENCIPHER_ONLY`` or ``KEY_AGREEMENT`` or ``KEY_ENCIPHERMENT`` or ``NON_REPUDIATION``
     */
    public String getKeyUsage() {
        return keyUsage;
    }

    public void setKeyUsage(String keyUsage) {
        this.keyUsage = keyUsage;
    }

    @Override
    protected List<CertificateDetail> findAllAws(AcmClient client) {
        return client.listCertificatesPaginator().certificateSummaryList().stream()
            .map(o -> client.describeCertificate(r -> r.certificateArn(o.certificateArn())).certificate())
            .collect(Collectors.toList());
    }

    @Override
    protected List<CertificateDetail> findAws(AcmClient client, Map<String, String> filters) {
        ListCertificatesRequest.Builder builder = ListCertificatesRequest.builder();
        if (filters.containsKey("certificate-status")) {
            builder = builder.certificateStatusesWithStrings(filters.get("certificate-status"));
        }

        Filters.Builder filterBuilder = Filters.builder();
        boolean filterPresent = false;
        if (filters.containsKey("extended-key-usage")) {
            filterPresent = true;
            filterBuilder = filterBuilder.extendedKeyUsageWithStrings(filters.get("extended-key-usage"));
        }

        if (filters.containsKey("key-algorithm")) {
            filterPresent = true;
            filterBuilder = filterBuilder.keyTypesWithStrings(filters.get("key-algorithm"));
        }

        if (filters.containsKey("key-usage")) {
            filterPresent = true;
            filterBuilder = filterBuilder.keyUsageWithStrings(filters.get("key-algorithm"));
        }

        if (filterPresent) {
            builder = builder.includes(filterBuilder.build());
        }

        return client.listCertificatesPaginator(builder.build()).certificateSummaryList().stream()
            .map(o -> client.describeCertificate(r -> r.certificateArn(o.certificateArn())).certificate())
            .collect(Collectors.toList());
    }
}
