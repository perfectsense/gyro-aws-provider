package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.Provider;

public class EksProvider extends Diffable implements Copyable<Provider> {

    private KmsKeyResource key; // special case id not arn

    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    @Override
    public void copyFrom(Provider model) {
        setKey(findById(KmsKeyResource.class, model.keyArn()));
    }

    @Override
    public String primaryKey() {
        return getKey().getArn();
    }

    Provider toProvider() {
        return Provider.builder().keyArn(getKey().getArn()).build();
    }
}
