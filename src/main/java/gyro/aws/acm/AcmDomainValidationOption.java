package gyro.aws.acm;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.acm.model.DomainValidation;
import software.amazon.awssdk.services.acm.model.DomainValidationOption;

public class AcmDomainValidationOption extends Diffable implements Copyable<DomainValidation> {
    private String domainName;
    private String validationDomain;

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

    @Override
    public void copyFrom(DomainValidation domainValidationOption) {
        setDomainName(domainValidationOption.domainName());
        setValidationDomain(domainValidationOption.validationDomain());

        //out put var
        /*domainValidationOption.resourceRecord();
        domainValidationOption.validationDomain();
        domainValidationOption.validationMethod();
        domainValidationOption.validationStatus();*/
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb  = new StringBuilder();

        sb.append("domain validation");

        if (!ObjectUtils.isBlank(getDomainName())) {
            sb.append(" with domain name - ").append(getDomainName());
        }

        if (!ObjectUtils.isBlank(getValidationDomain())) {
            sb.append(" with validation domain - ").append(getValidationDomain());
        }

        return sb.toString();
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
