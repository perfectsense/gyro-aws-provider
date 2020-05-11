package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionRule;

public class S3ServerSideEncryptionRule extends Diffable implements Copyable<ServerSideEncryptionRule> {

    private S3ServerSideEncryptionByDefault defaultEncryption;

    /**
     * The default server-side encryption to apply to new objects in the bucket. (Required)
     *
     * @subresource gyro.aws.s3.S3ServerSideEncryptionRule
     */
    @Required
    @Updatable
    public S3ServerSideEncryptionByDefault getDefaultEncryption() {
        return defaultEncryption;
    }

    public void setDefaultEncryption(S3ServerSideEncryptionByDefault defaultEncryption) {
        this.defaultEncryption = defaultEncryption;
    }

    @Override
    public void copyFrom(ServerSideEncryptionRule model) {
        S3ServerSideEncryptionByDefault encryptionByDefault = newSubresource(S3ServerSideEncryptionByDefault.class);
        encryptionByDefault.copyFrom(model.applyServerSideEncryptionByDefault());
        setDefaultEncryption(encryptionByDefault);
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ServerSideEncryptionRule toServerSideEncryptionRule() {
        return ServerSideEncryptionRule.builder().applyServerSideEncryptionByDefault(getDefaultEncryption().toServerSideEncryptionByDefault()).build();
    }
}
