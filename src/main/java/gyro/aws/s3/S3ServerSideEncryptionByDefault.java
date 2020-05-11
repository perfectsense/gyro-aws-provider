package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionByDefault;

public class S3ServerSideEncryptionByDefault extends Diffable implements Copyable<ServerSideEncryptionByDefault> {

    private KmsKeyResource key;
    private ServerSideEncryption encryptionType;

    /**
     * The KMS master key to use for the default encryption.
     */
    @Updatable
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    /**
     * The server-side encryption algorithm to use for the default encryption. (Required)
     */
    @Required
    @Updatable
    public ServerSideEncryption getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(ServerSideEncryption encryptionType) {
        this.encryptionType = encryptionType;
    }

    @Override
    public void copyFrom(ServerSideEncryptionByDefault model) {
        setKey(findById(KmsKeyResource.class, model.kmsMasterKeyID()));
        setEncryptionType(model.sseAlgorithm());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ServerSideEncryptionByDefault toServerSideEncryptionByDefault() {
        return ServerSideEncryptionByDefault.builder().sseAlgorithm(getEncryptionType()).kmsMasterKeyID(getKey().getArn()).build();
    }
}
