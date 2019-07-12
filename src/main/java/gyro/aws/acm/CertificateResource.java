package gyro.aws.acm;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.DescribeCertificateResponse;
import software.amazon.awssdk.services.acm.model.RequestCertificateResponse;
import software.amazon.awssdk.services.acm.model.ResourceNotFoundException;
import software.amazon.awssdk.services.acm.model.Tag;
import software.amazon.awssdk.services.acm.model.ValidationMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
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
        /*certificateDetail.createdAt();
        certificateDetail.extendedKeyUsages();
        certificateDetail.failureReason();
        certificateDetail.importedAt();
        certificateDetail.inUseBy();
        certificateDetail.issuer();
        certificateDetail.keyAlgorithmAsString();
        certificateDetail.keyUsages();
        certificateDetail.notAfter();
        certificateDetail.notBefore();
        certificateDetail.issuedAt();
        certificateDetail.renewalEligibility();
        certificateDetail.renewalSummary();
        certificateDetail.revocationReasonAsString();
        certificateDetail.revokedAt();
        certificateDetail.serial();
        certificateDetail.signatureAlgorithm();
        certificateDetail.status();
        certificateDetail.subject();
        certificateDetail.type();*/
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
    public void create() {
        AcmClient client = createClient(AcmClient.class);

        RequestCertificateResponse response = client.requestCertificate(
            r -> r.certificateAuthorityArn(getCertificateAuthorityArn())
                .domainName(getDomainName())
                .domainValidationOptions(getDomainValidationOption().stream().map(AcmDomainValidationOption::toDomainValidationOption).collect(Collectors.toList()))
                .idempotencyToken(UUID.randomUUID().toString())
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
    public void update(Resource current, Set<String> changedFieldNames) {
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
    public void delete() {
        AcmClient client = createClient(AcmClient.class);

        client.deleteCertificate(r -> r.certificateArn(getArn()));
    }

    @Override
    public String toDisplayString() {
        return "certificate";
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
