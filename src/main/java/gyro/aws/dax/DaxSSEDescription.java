package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.dax.model.SSEDescription;
import software.amazon.awssdk.services.dax.model.SSEStatus;

public class DaxSSEDescription extends Diffable implements Copyable<SSEDescription> {

    private SSEStatus status;

    @ValidStrings({ "ENABLING", "ENABLED", "DISABLING", "DISABLED" })
    public SSEStatus getStatus() {
        return status;
    }

    public void setStatus(SSEStatus status) {
        this.status = status;
    }

    @Override
    public void copyFrom(SSEDescription model) {
        setStatus(model.status());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
