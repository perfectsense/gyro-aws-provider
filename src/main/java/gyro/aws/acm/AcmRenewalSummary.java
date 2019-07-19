package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.RenewalSummary;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AcmRenewalSummary extends Diffable implements Copyable<RenewalSummary> {
    private Set<AcmDomainValidationOption> domainValidationOptions;
    private String renewalStatus;
    private String renewalStatusReason;
    private Date updatedAt;

    @Output
    public Set<AcmDomainValidationOption> getDomainValidationOptions() {
        if (domainValidationOptions == null) {
            domainValidationOptions = new HashSet<>();
        }

        return domainValidationOptions;
    }

    public void setDomainValidationOptions(Set<AcmDomainValidationOption> domainValidationOptions) {
        this.domainValidationOptions = domainValidationOptions;
    }

    @Output
    public String getRenewalStatus() {
        return renewalStatus;
    }

    public void setRenewalStatus(String renewalStatus) {
        this.renewalStatus = renewalStatus;
    }

    @Output
    public String getRenewalStatusReason() {
        return renewalStatusReason;
    }

    public void setRenewalStatusReason(String renewalStatusReason) {
        this.renewalStatusReason = renewalStatusReason;
    }

    @Output
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void copyFrom(RenewalSummary renewalSummary) {
        setRenewalStatus(renewalSummary.renewalStatusAsString());
        setRenewalStatusReason(renewalSummary.renewalStatusReasonAsString());
        setUpdatedAt(renewalSummary.updatedAt() != null ? Date.from(renewalSummary.updatedAt()) : null);
        setDomainValidationOptions(renewalSummary.domainValidationOptions().stream().map( o -> {
            AcmDomainValidationOption domainValidationOption = newSubresource(AcmDomainValidationOption.class);
            domainValidationOption.copyFrom(o);
            return domainValidationOption;
        }).collect(Collectors.toSet()));
    }

    @Override
    public String primaryKey() {
        return "renewal summary";
    }
}
