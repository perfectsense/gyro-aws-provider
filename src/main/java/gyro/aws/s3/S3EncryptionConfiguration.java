package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.EncryptionConfiguration;

public class S3EncryptionConfiguration extends Diffable implements Copyable<EncryptionConfiguration> {
    private KmsKeyResource kmsKey;

    /**
     * KMS Key ID or ALIAS ARN to encrypt objects with when replicated to destination. Required if SSE is enabled.
     */
    @Updatable
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    @Override
    public String primaryKey() {
        return "encryption configuration";
    }

    @Override
    public void copyFrom(EncryptionConfiguration encryptionConfiguration) {
        setKmsKey(findById(KmsKeyResource.class, encryptionConfiguration.replicaKmsKeyID()));
    }

    EncryptionConfiguration toEncryptionConfiguration() {
        return EncryptionConfiguration.builder()
                .replicaKmsKeyID(getKmsKey().getArn())
                .build();
    }
}
