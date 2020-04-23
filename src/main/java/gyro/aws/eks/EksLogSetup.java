package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.LogSetup;
import software.amazon.awssdk.services.eks.model.LogType;

public class EksLogSetup extends Diffable implements Copyable<LogSetup> {

    private LogType logTypes;
    private Boolean enabled;

    public LogType getLogTypes() {

        return logTypes;
    }

    public void setLogTypes(LogType logTypes) {
        this.logTypes = logTypes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(LogSetup model) {
        setEnabled(model.enabled());
        setLogTypes(model.types().get(0));
    }

    @Override
    public String primaryKey() {

        return logTypes.toString();
    }

    LogSetup toLogSeup() {
        return LogSetup.builder()
            .enabled(getEnabled())
            .types(getLogTypes())
            .build();
    }
}
