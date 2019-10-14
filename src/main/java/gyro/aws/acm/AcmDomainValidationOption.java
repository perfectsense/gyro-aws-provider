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

    /**
     * A fully qualified domain name (FQDN) in the certificate. (Required)
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * The domain name that ACM used to send domain validation emails. (Required)
     */
    public String getValidationDomain() {
        return validationDomain;
    }

    public void setValidationDomain(String validationDomain) {
        this.validationDomain = validationDomain;
    }

    /**
     * The domain validation method.
     */
    @Output
    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }

    /**
     * The validation status of the domain name.
     */
    @Output
    public DomainStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(DomainStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    /**
     * A list of email addresses that ACM used to send domain validation emails.
     */
    @Output
    public Set<String> getValidationEmails() {
        return validationEmails;
    }

    public void setValidationEmails(Set<String> validationEmails) {
        this.validationEmails = validationEmails;
    }

    /**
     * Contains the CNAME record that you add to your DNS database for domain validation.
     *
     * @subresource gyro.aws.acm.AcmResourceRecord
     */
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
