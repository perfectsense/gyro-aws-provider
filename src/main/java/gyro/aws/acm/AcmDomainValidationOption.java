package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.DomainStatus;
import software.amazon.awssdk.services.acm.model.DomainValidation;
import software.amazon.awssdk.services.acm.model.DomainValidationOption;
import software.amazon.awssdk.services.acm.model.ValidationMethod;

import java.util.HashSet;
import java.util.Set;

public class AcmDomainValidationOption extends Diffable implements Copyable<DomainValidation> {
    private String domainName;
    private String validationDomain;

    // --Output
    private ValidationMethod validationMethod;
    private DomainStatus validationStatus;
    private Set<String> validationEmails;
    private AcmResourceRecord resourceRecord;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getValidationDomain() {
        return validationDomain;
    }

    public void setValidationDomain(String validationDomain) {
        this.validationDomain = validationDomain;
    }

    @Output
    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }

    @Output
    public DomainStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(DomainStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Output
    public Set<String> getValidationEmails() {
        return validationEmails;
    }

    public void setValidationEmails(Set<String> validationEmails) {
        this.validationEmails = validationEmails;
    }

    @Output
    public AcmResourceRecord getResourceRecord() {
        return resourceRecord;
    }

    public void setResourceRecord(AcmResourceRecord resourceRecord) {
        this.resourceRecord = resourceRecord;
    }

    @Override
    public void copyFrom(DomainValidation domainValidationOption) {
        setDomainName(domainValidationOption.domainName());
        setValidationDomain(domainValidationOption.validationDomain());

        setValidationMethod(domainValidationOption.validationMethod());
        setValidationStatus(domainValidationOption.validationStatus());
        setValidationEmails(new HashSet<>(domainValidationOption.validationEmails()));

        if (domainValidationOption.resourceRecord() != null) {
            AcmResourceRecord acmResourceRecord = newSubresource(AcmResourceRecord.class);
            acmResourceRecord.copyFrom(domainValidationOption.resourceRecord());
            setResourceRecord(acmResourceRecord);
        }
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getDomainName(), getValidationDomain());
    }

    DomainValidationOption toDomainValidationOption() {
        return DomainValidationOption.builder()
            .domainName(getDomainName())
            .validationDomain(getValidationDomain())
            .build();
    }
}
