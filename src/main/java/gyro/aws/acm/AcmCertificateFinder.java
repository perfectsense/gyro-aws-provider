/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.acm;

import gyro.aws.AwsFinder;
import gyro.core.GyroException;
import gyro.core.Type;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.CertificateSummary;
import software.amazon.awssdk.services.acm.model.Filters;
import software.amazon.awssdk.services.acm.model.ListCertificatesRequest;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query acm-certificate.
 *
 * Example
 * -------
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
    private String arn;

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

    /**
     * The arn of the certificate. Specify more than one arn by separating with comma ",".
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<CertificateDetail> findAllAws(AcmClient client) {
        return client.listCertificatesPaginator().certificateSummaryList().stream()
            .map(o -> client.describeCertificate(r -> r.certificateArn(o.certificateArn())).certificate())
            .collect(Collectors.toList());
    }

    @Override
    protected List<CertificateDetail> findAws(AcmClient client, Map<String, String> filters) {
        if (filters.size() > 1 && filters.containsKey("arn")) {
            throw new GyroException("Cannot using any other filter when using 'arn' !!");
        }

        ListCertificatesRequest.Builder builder = ListCertificatesRequest.builder();
        boolean filterPresent = false;
        if (filters.containsKey("certificate-status")) {
            filterPresent = true;
            builder = builder.certificateStatusesWithStrings(filters.get("certificate-status"));
        }

        Filters.Builder filterBuilder = Filters.builder();
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

        Set<String> certArns = new LinkedHashSet<>();
        if (filterPresent) {
            builder = builder.includes(filterBuilder.build());

            certArns.addAll(client.listCertificatesPaginator(builder.build()).certificateSummaryList()
                .stream()
                .map(CertificateSummary::certificateArn)
                .collect(Collectors.toList()));
        }

        if (filters.containsKey("arn")) {
            certArns.addAll(Arrays.stream(filters.get("arn").split(","))
                .map(String::trim)
                .collect(Collectors.toList()));
        }

        return certArns.stream().map(o -> client.describeCertificate(r -> r.certificateArn(o)).certificate())
            .collect(Collectors.toList());
    }
}
