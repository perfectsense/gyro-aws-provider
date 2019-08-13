package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.Destination;
import software.amazon.awssdk.services.s3.model.OwnerOverride;
import software.amazon.awssdk.services.s3.model.StorageClass;

public class S3Destination extends Diffable implements Copyable<Destination> {
    private String account;
    private BucketResource bucket;
    private String encryptionConfiguration;
    private OwnerOverride ownerOverride;
    private StorageClass storageClass;

    /**
     * Account ID of destination bucket. Required if the source and destination buckets are not the same.
     */
    @Updatable
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * ARN of the destination bucket. (Required)
     */
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * KMS Key ID or ALIAS ARN to encrypt objects with when replicated to destination. Required if SSE is enabled.
     */
    @Updatable
    public String getEncryptionConfiguration() {
        return encryptionConfiguration;
    }

    public void setEncryptionConfiguration(String encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    /**
     * Sets the ownership of the replica. Valid value is ``DESTINATION``
     */
    @Updatable
    public OwnerOverride getOwnerOverride() {
        return ownerOverride;
    }

    public void setOwnerOverride(OwnerOverride ownerOverride) {
        this.ownerOverride = ownerOverride;
    }

    /**
     * Storage class for replicated object. Defaults to class of source object. Valid values are ``DEEP_ARCHIVE`` or ``GLACIER`` or ``INTELLIGENT_TIERING`` or ``ONEZONE_IA`` or ``REDUCED_REDUNDANCY`` or ``STANDARD`` or ``STANDARD_IA``
     */
    @Updatable
    public StorageClass getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    private String getBucketNameFromArn(String bucketArn){
        return bucketArn.split(":")[5];
    }

    @Override
    public void copyFrom(Destination destination) {
        setAccount(destination.account());
        setBucket(findById(BucketResource.class, getBucketNameFromArn(destination.bucket())));
        setStorageClass(destination.storageClass());

        if (destination.encryptionConfiguration() != null){
            setEncryptionConfiguration(destination.encryptionConfiguration().replicaKmsKeyID());
        } else {
            encryptionConfiguration = null;
        }

        if (destination.accessControlTranslation() != null) {
            setOwnerOverride(destination.accessControlTranslation().owner());
        } else {
            setOwnerOverride(null);
        }
    }

    Destination toDestination(){
        Destination.Builder builder = Destination.builder()
                .account(getAccount())
                .bucket("arn:aws:s3:::" + getBucket().getName());

        if (getEncryptionConfiguration() != null){
            builder.encryptionConfiguration(
                c -> c.replicaKmsKeyID(getEncryptionConfiguration())
            );
        }

        if (getOwnerOverride() != null){
            builder.accessControlTranslation(
                    t -> t.owner(getOwnerOverride())
            );
        }

        if (getStorageClass() != null){
            builder.storageClass(getStorageClass());
        }

        return builder.build();
    }
}
