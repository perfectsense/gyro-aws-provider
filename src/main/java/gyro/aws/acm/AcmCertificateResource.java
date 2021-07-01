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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.acmpca.AcmPcaCertificateAuthority;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.CertificateStatus;
import software.amazon.awssdk.services.acm.model.CertificateType;
import software.amazon.awssdk.services.acm.model.DescribeCertificateResponse;
import software.amazon.awssdk.services.acm.model.FailureReason;
import software.amazon.awssdk.services.acm.model.InvalidStateException;
import software.amazon.awssdk.services.acm.model.RenewalEligibility;
import software.amazon.awssdk.services.acm.model.RequestCertificateResponse;
import software.amazon.awssdk.services.acm.model.ResourceNotFoundException;
import software.amazon.awssdk.services.acm.model.Tag;
import software.amazon.awssdk.services.acm.model.ValidationMethod;

/**
 * Creates a ACM Certificate.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::acm-certificate acm-certificate-example
 *         domain-name: "gyro-test.beam-sandbox.psdops.com"
 *         domain-validation-option
 *             domain-name: "gyro-test.beam-sandbox.psdops.com"
 *             validation-domain: "beam-sandbox.psdops.com"
 *         end
 *
 *         options
 *             preference: DISABLED
 *         end
 *
 *         validation-method: DNS
 *
 *         tags: {
 *             Name: "acm-certificate-example
 *         }
 *     end
 */
@Type("acm-certificate")
public class AcmCertificateResource extends AwsResource implements Copyable<CertificateDetail> {
    private AcmPcaCertificateAuthority certificateAuthority;
    private String domainName;
    private Set<AcmDomainValidationOption> domainValidationOption;
    private AcmCertificateOptions options;
    private Set<String> subjectAlternativeNames;
    private ValidationMethod validationMethod;
    private Map<String, String> tags;

    // --Output
    private String arn;
    private Date createdAt;
    private Set<AcmExtendedKeyUsage> extendedKeyUsages;
    private FailureReason failureReason;
    private Set<String> inUseBy;
    private String keyAlgorithm;
    private Set<AcmKeyUsage> keyUsages;
    private Date importedAt;
    private Date issuedAt;
    private String issuer;
    private Date notAfter;
    private Date notBefore;
    private Date revokedAt;
    private RenewalEligibility renewalEligibility;
    private AcmRenewalSummary renewalSummary;
    private String revocationReason;
    private String serial;
    private String signatureAlgorithm;
    private String subject;
    private CertificateStatus status;
    private CertificateType type;

    /**
     * The certificate Authority to create the ACM certificate under.
     */
    public AcmPcaCertificateAuthority getCertificateAuthority() {
        return certificateAuthority;
    }

    public void setCertificateAuthority(AcmPcaCertificateAuthority certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    /**
     * Fully qualified domain name (FQDN), that you want to secure with an ACM certificate.
     */
    @Required
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * The domain validation option that you want ACM to use to send you emails so that you can validate domain ownership.
     *
     * @subresource gyro.aws.acm.AcmDomainValidationOption
     */
    @Required
    public Set<AcmDomainValidationOption> getDomainValidationOption() {
        return domainValidationOption;
    }

    public void setDomainValidationOption(Set<AcmDomainValidationOption> domainValidationOption) {
        this.domainValidationOption = domainValidationOption;
    }

    /**
     * Set certificate options for the ACM.
     *
     * @subresource gyro.aws.acm.AcmCertificateOptions
     */
    @Updatable
    public AcmCertificateOptions getOptions() {
        if (options == null) {
            options = newSubresource(AcmCertificateOptions.class);
        }

        return options;
    }

    public void setOptions(AcmCertificateOptions options) {
        this.options = options;
    }

    /**
     * Additional FQDNs to be included in the Subject Alternative Name extension of the ACM certificate.
     */
    public Set<String> getSubjectAlternativeNames() {
        return subjectAlternativeNames;
    }

    public void setSubjectAlternativeNames(Set<String> subjectAlternativeNames) {
        this.subjectAlternativeNames = subjectAlternativeNames;
    }

    /**
     * The method you want to use if you are requesting a public certificate to validate that you own or control domain. Defaults to ``DNS``
     */
    @ValidStrings({"DNS", "EMAIL"})
    public ValidationMethod getValidationMethod() {
        if (validationMethod == null) {
            validationMethod = ValidationMethod.DNS;
        }

        return validationMethod;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }

    /**
     * Set tags for the ACM.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Amazon Resource Name (ARN) of the certificate.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The time at which the certificate was requested.
     */
    @Output
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * A list of Extended Key Usage
     */
    @Output
    public Set<AcmExtendedKeyUsage> getExtendedKeyUsages() {
        return extendedKeyUsages;
    }

    public void setExtendedKeyUsages(Set<AcmExtendedKeyUsage> extendedKeyUsages) {
        this.extendedKeyUsages = extendedKeyUsages;
    }

    /**
     * The reason the certificate request failed.
     */
    @Output
    public FailureReason getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(FailureReason failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * A Set of ARNs for the AWS resources that are using the certificate.
     */
    @Output
    public Set<String> getInUseBy() {
        return inUseBy;
    }

    public void setInUseBy(Set<String> inUseBy) {
        this.inUseBy = inUseBy;
    }

    /**
     * The algorithm that was used to generate the public-private key pair.
     */
    @Output
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * A Set of Key Usage
     *
     * @subresource gyro.aws.acm.AcmKeyUsage
     */
    @Output
    public Set<AcmKeyUsage> getKeyUsages() {
        return keyUsages;
    }

    public void setKeyUsages(Set<AcmKeyUsage> keyUsages) {
        this.keyUsages = keyUsages;
    }

    /**
     * The date and time at which the certificate was imported.
     */
    @Output
    public Date getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Date importedAt) {
        this.importedAt = importedAt;
    }

    /**
     * The time at which the certificate was issued.
     */
    @Output
    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    /**
     * The name of the certificate authority that issued and signed the certificate.
     */
    @Output
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * The time after which the certificate is not valid.
     */
    @Output
    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    /**
     * The time before which the certificate is not valid.
     */
    @Output
    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    /**
     * The time at which the certificate was revoked.
     */
    @Output
    public Date getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Date revokedAt) {
        this.revokedAt = revokedAt;
    }

    /**
     * Specifies whether the certificate is eligible for renewal.
     */
    @Output
    public RenewalEligibility getRenewalEligibility() {
        return renewalEligibility;
    }

    public void setRenewalEligibility(RenewalEligibility renewalEligibility) {
        this.renewalEligibility = renewalEligibility;
    }

    /**
     * The information about the status of ACM's managed renewal for the certificate.
     *
     * @subresource gyro.aws.acm.AcmRenewalSummary
     */
    @Output
    public AcmRenewalSummary getRenewalSummary() {
        return renewalSummary;
    }

    public void setRenewalSummary(AcmRenewalSummary renewalSummary) {
        this.renewalSummary = renewalSummary;
    }

    /**
     * The reason the certificate was revoked.
     */
    @Output
    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    /**
     * The serial number of the certificate.
     */
    @Output
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**
     * The algorithm that was used to sign the certificate.
     */
    @Output
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    /**
     * The name of the entity that is associated with the public key contained in the certificate.
     */
    @Output
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * The status of the certificate.
     */
    @Output
    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    /**
     * The source of the certificate.
     */
    @Output
    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(CertificateDetail certificateDetail) {
        setCertificateAuthority(!ObjectUtils.isBlank(certificateDetail.certificateAuthorityArn()) ? findById(AcmPcaCertificateAuthority.class, certificateDetail.certificateAuthorityArn()) : null);
        setDomainName(certificateDetail.domainName());
        setSubjectAlternativeNames(new HashSet<>(certificateDetail.subjectAlternativeNames()));
        setDomainValidationOption(certificateDetail.domainValidationOptions().stream().map(o -> {
            AcmDomainValidationOption acmDomainValidationOption = newSubresource(AcmDomainValidationOption.class);
            acmDomainValidationOption.copyFrom(o);
            return acmDomainValidationOption;
        }).collect(Collectors.toSet()));
        AcmCertificateOptions options = newSubresource(AcmCertificateOptions.class);
        options.copyFrom(certificateDetail.options());
        setOptions(options);
        setArn(certificateDetail.certificateArn());
        setCreatedAt(certificateDetail.createdAt() != null ? Date.from(certificateDetail.createdAt()) : null);
        setExtendedKeyUsages(certificateDetail.extendedKeyUsages().stream().map(o -> {
            AcmExtendedKeyUsage extendedKeyUsage = newSubresource(AcmExtendedKeyUsage.class);
            extendedKeyUsage.copyFrom(o);
            return extendedKeyUsage;
        }).collect(Collectors.toSet()));
        setFailureReason(certificateDetail.failureReason());
        setInUseBy(new HashSet<>(certificateDetail.inUseBy()));
        setKeyAlgorithm(certificateDetail.keyAlgorithmAsString());
        setKeyUsages(certificateDetail.keyUsages().stream().map(o -> {
            AcmKeyUsage keyUsage = newSubresource(AcmKeyUsage.class);
            keyUsage.copyFrom(o);
            return keyUsage;
        }).collect(Collectors.toSet()));
        setImportedAt(certificateDetail.importedAt() != null ? Date.from(certificateDetail.importedAt()) : null);
        setIssuedAt(certificateDetail.issuedAt() != null ? Date.from(certificateDetail.issuedAt()) : null);
        setIssuer(certificateDetail.issuer());
        setNotAfter(certificateDetail.notAfter() != null ? Date.from(certificateDetail.notAfter()) : null);
        setNotBefore(certificateDetail.notBefore() != null ? Date.from(certificateDetail.notBefore()) : null);
        setRevokedAt(certificateDetail.revokedAt() != null ? Date.from(certificateDetail.revokedAt()) : null);
        setRenewalEligibility(certificateDetail.renewalEligibility());

        if (certificateDetail.renewalSummary() != null) {
            AcmRenewalSummary renewalSummary = newSubresource(AcmRenewalSummary.class);
            renewalSummary.copyFrom(certificateDetail.renewalSummary());
            setRenewalSummary(renewalSummary);
        } else {
            setRenewalSummary(null);
        }

        setRevocationReason(certificateDetail.revocationReasonAsString());
        setSerial(certificateDetail.serial());
        setSignatureAlgorithm(certificateDetail.signatureAlgorithm());
        setSubject(certificateDetail.subject());
        setStatus(certificateDetail.status());
        setType(certificateDetail.type());
    }

    @Override
    public boolean refresh() {
        AcmClient client = createClient(AcmClient.class);

        try {
            DescribeCertificateResponse response = client.describeCertificate(r -> r.certificateArn(getArn()));

            copyFrom(response.certificate());

            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        AcmClient client = createClient(AcmClient.class);

        RequestCertificateResponse response = client.requestCertificate(
            r -> r.certificateAuthorityArn(
                getCertificateAuthority() != null ? getCertificateAuthority().getArn() : null)
                .domainName(getDomainName())
                .domainValidationOptions(getDomainValidationOption().stream()
                    .map(AcmDomainValidationOption::toDomainValidationOption)
                    .collect(Collectors.toList()))
                .idempotencyToken(UUID.randomUUID().toString().replaceAll("-", ""))
                .options(getOptions().toCertificateOptions())
                .subjectAlternativeNames(getSubjectAlternativeNames())
                .validationMethod(getValidationMethod())
        );

        setArn(response.certificateArn());

        if (getDomainValidationOption() != null) {
            Wait.atMost(10, TimeUnit.SECONDS)
                .checkEvery(5, TimeUnit.SECONDS)
                .until(() -> {
                    CertificateDetail certificate = client.describeCertificate(r -> r.certificateArn(getArn()))
                        .certificate();
                    return certificate != null && certificate.domainValidationOptions() != null
                        && !certificate.domainValidationOptions().isEmpty();
                });
        }

        refresh();

        if (!getTags().isEmpty()) {
            saveTags(client, new HashMap<>());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AcmClient client = createClient(AcmClient.class);

        if (changedFieldNames.contains("options")) {

            try {
                client.updateCertificateOptions(
                    r -> r.certificateArn(getArn())
                        .options(getOptions().toCertificateOptions())
                );
            } catch (InvalidStateException ex) {
                throw new GyroException("The ACM Certificate cannot be updated in its current state - " + getStatus().toString());
            }
        }

        if (changedFieldNames.contains("tags")) {
            saveTags(client, ((AcmCertificateResource) current).getTags());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AcmClient client = createClient(AcmClient.class);

        client.deleteCertificate(r -> r.certificateArn(getArn()));
    }

    private void saveTags(AcmClient client, Map<String, String> oldTags) {
        MapDifference<String,String> diff = Maps.difference(getTags(), oldTags);

        List<Tag> deleteTags = new ArrayList<>();
        List<Tag> addTags = new ArrayList<>();

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            deleteTags.addAll(diff.entriesOnlyOnRight().keySet().stream()
                .map(o -> Tag.builder().key(o).value(diff.entriesOnlyOnRight().get(o)).build())
                .collect(Collectors.toList()));
        }

        if (!diff.entriesDiffering().isEmpty()) {
            deleteTags.addAll(diff.entriesDiffering().keySet().stream()
                .map(o -> Tag.builder().key(o).value(diff.entriesDiffering().get(o).rightValue()).build())
                .collect(Collectors.toList()));

            addTags.addAll(diff.entriesDiffering().keySet().stream()
                .map(o -> Tag.builder().key(o).value(diff.entriesDiffering().get(o).leftValue()).build())
                .collect(Collectors.toList()));
        }

        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            addTags.addAll(diff.entriesOnlyOnLeft().keySet().stream()
                .map(o -> Tag.builder().key(o).value(diff.entriesOnlyOnLeft().get(o)).build())
                .collect(Collectors.toList()));
        }

        if (!deleteTags.isEmpty()) {
            client.removeTagsFromCertificate(r -> r.certificateArn(getArn()).tags(deleteTags));
        }

        if (!addTags.isEmpty()) {
            client.addTagsToCertificate(r -> r.certificateArn(getArn()).tags(addTags));
        }
    }
}
