package gyro.aws.kms;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.kms.model.MultiRegionConfiguration;
import software.amazon.awssdk.services.kms.model.MultiRegionKey;

import java.util.ArrayList;
import java.util.List;

public class KmsKeyMultiRegionConfiguration extends Diffable implements Copyable<MultiRegionConfiguration> {

    private String multiRegionKeyType;

    private KmsKeyMultiRegionKey primaryKey;

    private List<KmsKeyMultiRegionKey> replicaKeys;

    @Override
    public void copyFrom(MultiRegionConfiguration model) {
        KmsKeyMultiRegionKey multiRegionKey = newSubresource(KmsKeyMultiRegionKey.class);
        multiRegionKey.copyFrom(model.primaryKey());

        setPrimaryKey(multiRegionKey);

        List<KmsKeyMultiRegionKey> replicaKeys = new ArrayList<>();
        for (MultiRegionKey replicaKey : model.replicaKeys()) {
            KmsKeyMultiRegionKey regionSpecificKey = newSubresource(KmsKeyMultiRegionKey.class);
            regionSpecificKey.copyFrom(replicaKey);
            replicaKeys.add(regionSpecificKey);
        }

        setReplicaKeys(replicaKeys);
        setMultiRegionKeyType(model.multiRegionKeyTypeAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public String getMultiRegionKeyType() {
        return multiRegionKeyType;
    }

    public void setMultiRegionKeyType(String multiRegionKeyType) {
        this.multiRegionKeyType = multiRegionKeyType;
    }

    public KmsKeyMultiRegionKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(KmsKeyMultiRegionKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<KmsKeyMultiRegionKey> getReplicaKeys() {
        return replicaKeys;
    }

    public void setReplicaKeys(List<KmsKeyMultiRegionKey> replicaKeys) {
        this.replicaKeys = replicaKeys;
    }
}
