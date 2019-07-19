package gyro.aws.acm;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.CertificateStatus;
import software.amazon.awssdk.services.acm.model.CertificateType;
import software.amazon.awssdk.services.acm.model.DescribeCertificateResponse;
import software.amazon.awssdk.services.acm.model.FailureReason;
import software.amazon.awssdk.services.acm.model.RenewalEligibility;
import software.amazon.awssdk.services.acm.model.RequestCertificateResponse;
import software.amazon.awssdk.services.acm.model.ResourceNotFoundException;
import software.amazon.awssdk.services.acm.model.Tag;
import software.amazon.awssdk.services.acm.model.ValidationMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Type("acm-certificate")
public class CertificateResource extends AwsResource implements Copyable<CertificateDetail> {
    private String certificateAuthorityArn;
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

    public String getCertificateAuthorityArn() {
        return certificateAuthorityArn;
    }

    public void setCertificateAuthorityArn(String certificateAuthorityArn) {
        this.certificateAuthorityArn = certificateAuthorityArn;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public Set<AcmDomainValidationOption> getDomainValidationOption() {
        return domainValidationOption;
    }

    public void setDomainValidationOption(Set<AcmDomainValidationOption> domainValidationOption) {
        this.domainValidationOption = domainValidationOption;
    }

    public AcmCertificateOptions getOptions() {
        return options;
    }

    public void setOptions(AcmCertificateOptions options) {
        this.options = options;
    }

    public Set<String> getSubjectAlternativeNames() {
        return subjectAlternativeNames;
    }

    public void setSubjectAlternativeNames(Set<String> subjectAlternativeNames) {
        this.subjectAlternativeNames = subjectAlternativeNames;
    }

    public ValidationMethod getValidationMethod() {
        if (validationMethod == null) {
            validationMethod = ValidationMethod.DNS;
        }

        return validationMethod;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }

    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Output
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Output
    public Set<AcmExtendedKeyUsage> getExtendedKeyUsages() {
        return extendedKeyUsages;
    }

    public void setExtendedKeyUsages(Set<AcmExtendedKeyUsage> extendedKeyUsages) {
        this.extendedKeyUsages = extendedKeyUsages;
    }

    @Output
    public FailureReason getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(FailureReason failureReason) {
        this.failureReason = failureReason;
    }

    @Output
    public Set<String> getInUseBy() {
        return inUseBy;
    }

    public void setInUseBy(Set<String> inUseBy) {
        this.inUseBy = inUseBy;
    }

    @Output
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @Output
    public Set<AcmKeyUsage> getKeyUsages() {
        return keyUsages;
    }

    public void setKeyUsages(Set<AcmKeyUsage> keyUsages) {
        this.keyUsages = keyUsages;
    }

    @Output
    public Date getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Date importedAt) {
        this.importedAt = importedAt;
    }

    @Output
    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Output
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Output
    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    @Output
    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    @Output
    public Date getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Date revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Output
    public RenewalEligibility getRenewalEligibility() {
        return renewalEligibility;
    }

    public void setRenewalEligibility(RenewalEligibility renewalEligibility) {
        this.renewalEligibility = renewalEligibility;
    }

    @Output
    public AcmRenewalSummary getRenewalSummary() {
        return renewalSummary;
    }

    public void setRenewalSummary(AcmRenewalSummary renewalSummary) {
        this.renewalSummary = renewalSummary;
    }

    @Output
    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    @Output
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Output
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Output
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Output
    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    @Output
    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(CertificateDetail certificateDetail) {
        setCertificateAuthorityArn(certificateDetail.certificateAuthorityArn());
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



        //output var
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
            r -> r.certificateAuthorityArn(getCertificateAuthorityArn())
                .domainName(getDomainName())
                .domainValidationOptions(getDomainValidationOption().stream().map(AcmDomainValidationOption::toDomainValidationOption).collect(Collectors.toList()))
                .idempotencyToken(UUID.randomUUID().toString().replaceAll("-",""))
                .options(getOptions().toCertificateOptions())
                .subjectAlternativeNames(getSubjectAlternativeNames())
                .validationMethod(getValidationMethod())
        );

        setArn(response.certificateArn());

        if (!getTags().isEmpty()) {
            saveTags(client, new HashMap<>());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AcmClient client = createClient(AcmClient.class);

        if (changedFieldNames.contains("options")) {

            client.updateCertificateOptions(
                r -> r.certificateArn(getArn())
                    .options(getOptions().toCertificateOptions())
            );
        }

        if (changedFieldNames.contains("tags")) {
            saveTags(client, ((CertificateResource) current).getTags());
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
