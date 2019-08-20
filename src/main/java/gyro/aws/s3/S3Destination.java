package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.Destination;
import software.amazon.awssdk.services.s3.model.StorageClass;

public class S3Destination extends Diffable implements Copyable<Destination> {
    private String account;
    private BucketResource bucket;
    private S3EncryptionConfiguration encryptionConfiguration;
    private S3AccessControlTranslation accessControlTranslation;
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
     * Sets the KMS key for the destination bucket.
     */
    @Updatable
    public S3EncryptionConfiguration getEncryptionConfiguration() {
        return encryptionConfiguration;
    }

    public void setEncryptionConfiguration(S3EncryptionConfiguration encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    /**
     * Changes the owner of the replicated object to the destination bucket in cross account scenarios. Account is required if this value is set.
     */
    @Updatable
    public S3AccessControlTranslation getAccessControlTranslation() {
        return accessControlTranslation;
    }

    public void setAccessControlTranslation(S3AccessControlTranslation accessControlTranslation) {
        this.accessControlTranslation = accessControlTranslation;
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

    private String getBucketNameFromArn(String bucketArn) {
        return bucketArn.split(":")[5];
    }

    @Override
    public String primaryKey() {
        return getBucket().getName();
    }

    @Override
    public void copyFrom(Destination destination) {
        setAccount(destination.account());
        setBucket(findById(BucketResource.class, getBucketNameFromArn(destination.bucket())));
        setStorageClass(destination.storageClass());

        if (destination.encryptionConfiguration() != null) {
            S3EncryptionConfiguration s3EncryptionConfiguration = newSubresource(S3EncryptionConfiguration.class);
            s3EncryptionConfiguration.copyFrom(destination.encryptionConfiguration());

            setEncryptionConfiguration(s3EncryptionConfiguration);
        }

        if (destination.accessControlTranslation() != null) {
            S3AccessControlTranslation s3AccessControlTranslation = newSubresource(S3AccessControlTranslation.class);
            s3AccessControlTranslation.copyFrom(destination.accessControlTranslation());

            setAccessControlTranslation(s3AccessControlTranslation);
        }
    }

    Destination toDestination() {
        Destination.Builder builder = Destination.builder()
                .account(getAccount())
                .bucket("arn:aws:s3:::" + getBucket().getName());

        if (getEncryptionConfiguration() != null) {
            builder.encryptionConfiguration(getEncryptionConfiguration().toEncryptionConfiguration());
        }

        if (getAccessControlTranslation() != null) {
            builder.accessControlTranslation(getAccessControlTranslation().toAccessControlTranslation());
        }

        if (getStorageClass() != null) {
            builder.storageClass(getStorageClass());
        }

        return builder.build();
    }
}