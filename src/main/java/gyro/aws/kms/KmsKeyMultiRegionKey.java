package gyro.aws.kms;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.kms.model.MultiRegionKey;

public class KmsKeyMultiRegionKey extends Diffable implements Copyable<MultiRegionKey> {

    private String arn;
    private String region;

    /**
     * The Amazon Resource Name (ARN) associated with the key.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The AWS region associated with the key.
     */
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void copyFrom(MultiRegionKey model) {
        setArn(model.arn());
        setRegion(model.region());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
