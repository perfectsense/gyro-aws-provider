package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.EncryptionConfig;

public class EksEncryptionConfig extends Diffable implements Copyable<EncryptionConfig> {

    private EksProvider provider;

    public EksProvider getProvider() {
        return provider;
    }

    public void setProvider(EksProvider provider) {
        this.provider = provider;
    }

    @Override
    public void copyFrom(EncryptionConfig model) {
        EksProvider eksProvider = newSubresource(EksProvider.class);
        eksProvider.copyFrom(model.provider());
        setProvider(eksProvider);
    }

    @Override
    public String primaryKey() {
        return null;
    }

    EncryptionConfig toEncryptionConfig() {
        return EncryptionConfig.builder().provider(getProvider().toProvider()).resources("secrets").build();
    }
}
