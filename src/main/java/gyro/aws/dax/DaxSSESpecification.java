package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.SSESpecification;

public class DaxSSESpecification extends Diffable implements Copyable<SSESpecification> {

    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(SSESpecification model) {
        setEnabled(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public SSESpecification toSseSpecification() {
        return SSESpecification.builder()
            .enabled(getEnabled())
            .build();
    }
}
