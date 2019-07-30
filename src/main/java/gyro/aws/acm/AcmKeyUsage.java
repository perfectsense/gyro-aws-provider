package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.KeyUsage;
import software.amazon.awssdk.services.acm.model.KeyUsageName;

public class AcmKeyUsage extends Diffable implements Copyable<KeyUsage> {
    private KeyUsageName name;

    /**
     * Key Usage extension name.
     */
    @Output
    public KeyUsageName getName() {
        return name;
    }

    public void setName(KeyUsageName name) {
        this.name = name;
    }

    @Override
    public void copyFrom(KeyUsage keyUsage) {
        setName(keyUsage.name());
    }

    @Override
    public String primaryKey() {
        return getName() != null ? getName().toString() : "";
    }
}
