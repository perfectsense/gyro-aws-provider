package gyro.aws.secretsmanager;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.secretsmanager.model.RotationRulesType;

public class SecretRotationRulesType extends Diffable implements Copyable<RotationRulesType> {

    private Long automaticallyAfterDays;

    /**
     * The number of days between automatic scheduled rotations of the secret.
     */
    @Output
    public Long getAutomaticallyAfterDays() {
        return automaticallyAfterDays;
    }

    public void setAutomaticallyAfterDays(Long automaticallyAfterDays) {
        this.automaticallyAfterDays = automaticallyAfterDays;
    }

    @Override
    public void copyFrom(RotationRulesType model) {
        setAutomaticallyAfterDays(model.automaticallyAfterDays());
    }

    @Override
    public String primaryKey() {
        return "rotation rules type";
    }
}
