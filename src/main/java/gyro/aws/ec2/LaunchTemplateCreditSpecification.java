package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.CreditSpecification;
import software.amazon.awssdk.services.ec2.model.CreditSpecificationRequest;

public class LaunchTemplateCreditSpecification extends Diffable implements Copyable<CreditSpecification> {

    private String cpuCredits;

    /**
     * The credit option for CPU usage of a ``t2``, ``t3``, or ``t3a`` instance. Valid values are ``standard`` and ``unlimited``.
     */
    @Required
    @Updatable
    @ValidStrings({ "standard", "unlimited" })
    public String getCpuCredits() {
        return cpuCredits;
    }

    public void setCpuCredits(String cpuCredits) {
        this.cpuCredits = cpuCredits;
    }

    @Override
    public void copyFrom(CreditSpecification model) {
        setCpuCredits(model.cpuCredits());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CreditSpecificationRequest toCreditSpecification() {
        return CreditSpecificationRequest.builder().cpuCredits(getCpuCredits()).build();
    }
}
