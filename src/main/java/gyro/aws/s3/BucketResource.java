package gyro.aws.s3;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketAccelerateStatus;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketRequestPaymentResponse;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.Payer;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an S3 bucket with enabled/disabled object lock.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::s3-bucket bucket
 *         name: bucket-example
 *         enable-object-lock: true
 *         tags:
 *             Name: bucket-example-update
 *         end
 *         enable-accelerate-config: true
 *         enable-version: true
 *         enable-pay: false
 *     end
 *
 * Example with cors rule
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::s3-bucket bucket
 *         name: bucket-example-with-cors
 *         enable-object-lock: true
 *         tags:
 *             Name: bucket-example-update
 *         end
 *         enable-accelerate-config: true
 *         enable-version: true
 *         enable-pay: false
 *
 *         cors-rule
 *             allowed-origins: [
 *                 "*"
 *             ]
 *             allowed-methods: [
 *                 "PUT"
 *             ]
 *             max-age-seconds: 300
 *         end
 *     end
 *
 * Example with life cycle rule
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::s3-bucket bucket
 *         name: bucket-example-with-lifecycle
 *         enable-object-lock: true
 *         tags:
 *             Name: bucket-example-update
 *         end
 *         enable-accelerate-config: true
 *         enable-version: true
 *         enable-pay: false
 *
 *         lifecycle-rule
 *             lifecycle-rule-name: "lifecycle-rule-name"
 *             expired-object-delete-marker: false
 *             status: "Disabled"
 *
 *             transition
 *                 days: 40
 *                 storage-class: "STANDARD_IA"
 *             end
 *
 *             non-current-transition
 *                 days: 40
 *                 storage-class: "STANDARD_IA"
 *             end
 *
 *             non-current-version-expiration-days: 403
 *             incomplete-multipart-upload-days: 5
 *         end
 *     end
 */
@Type("s3-bucket")
public class BucketResource extends AwsResource implements Copyable<Bucket> {

    private String name;
    private Boolean enableObjectLock;
    private Map<String, String> tags;
    private Boolean enableAccelerateConfig;
    private Boolean enableVersion;
    private Boolean enablePay;
    private List<S3CorsRule> corsRule;
    private List<S3LifecycleRule> lifecycleRule;

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Enable object lock property for the bucket which prevents objects from being deleted. Can only be set during creation. See `S3 Object Lock <https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html/>`_.
     */
    public Boolean getEnableObjectLock() {
        if (enableObjectLock == null) {
            enableObjectLock = false;
        }

        return enableObjectLock;
    }

    public void setEnableObjectLock(Boolean enableObjectLock) {
        this.enableObjectLock = enableObjectLock;
    }

    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * Enable fast easy and secure transfers of files to and from the bucket. See `S3 Transfer Acceleration <https://docs.aws.amazon.com/AmazonS3/latest/dev/transfer-acceleration.html/>`_.
     */
    @Updatable
    public Boolean getEnableAccelerateConfig() {
        if (enableAccelerateConfig == null) {
            enableAccelerateConfig = false;
        }

        return enableAccelerateConfig;
    }

    public void setEnableAccelerateConfig(Boolean enableAccelerateConfig) {
        this.enableAccelerateConfig = enableAccelerateConfig;
    }

    /**
     * Enable keeping multiple versions of an object in the same bucket. See `S3 Versioning <https://docs.aws.amazon.com/AmazonS3/latest/user-guide/enable-versioning.html/>`_. Updatable only when object lock is disabled.
     */
    @Updatable
    public Boolean getEnableVersion() {
        if (enableVersion == null) {
            enableVersion = false;
        }

        return enableVersion;
    }

    public void setEnableVersion(Boolean enableVersion) {
        this.enableVersion = enableVersion;
    }

    /**
     * Enable the requester to pay for requests to the bucket than the owner. See `S3 Requester Pays Bucket <https://docs.aws.amazon.com/AmazonS3/latest/dev/RequesterPaysBuckets.html/>`_.
     */
    @Updatable
    public Boolean getEnablePay() {
        if (enablePay == null) {
            enablePay = false;
        }

        return enablePay;
    }

    public void setEnablePay(Boolean enablePay) {
        this.enablePay = enablePay;
    }

    /**
     * Configure the cross origin request policy for the bucket.
     *
     * @subresource gyro.aws.s3.S3CorsRule
     */
    @Updatable
    public List<S3CorsRule> getCorsRule() {
        if (corsRule == null) {
            corsRule = new ArrayList<>();
        }

        return corsRule;
    }

    public void setCorsRule(List<S3CorsRule> corsRule) {
        this.corsRule = corsRule;
    }

    /**
     * Configure the cross origin request policy for the bucket.
     *
     * @subresource gyro.aws.s3.S3LifecycleRule
     */
    @Updatable
    public List<S3LifecycleRule> getLifecycleRule() {
        if (lifecycleRule == null) {
            lifecycleRule = new ArrayList<>();
        }

        return lifecycleRule;
    }

    public void setLifecycleRule(List<S3LifecycleRule> lifecycleRule) {
        this.lifecycleRule = lifecycleRule;
    }

    @Override
    public void copyFrom(Bucket bucket) {
        S3Client client = createClient(S3Client.class);
        setName(bucket.name());
        loadTags(client);
        loadAccelerateConfig(client);
        loadEnableVersion(client);
        loadEnablePay(client);
        loadCorsRules(client);
        loadLifecycleRules(client);
    }

    @Override
    public boolean refresh() {
        S3Client client = createClient(S3Client.class);

        Bucket bucket = getBucket(client);

        if (bucket == null) {
            return false;
        }

        this.copyFrom(bucket);

        return true;
    }

    @Override
    public void create() {
        S3Client client = createClient(S3Client.class);
        client.createBucket(
            r -> r.bucket(getName())
                .objectLockEnabledForBucket(getEnableObjectLock())
        );

        if (!getTags().isEmpty()) {
            saveTags(client);
        }

        if (getEnableAccelerateConfig()) {
            saveAccelerateConfig(client);
        }

        if (getEnableVersion()) {
            saveEnableVersion(client);
        }

        if (getEnablePay()) {
            saveEnablePay(client);
        }

        if (!getCorsRule().isEmpty()) {
            saveCorsRules(client);
        }

        if (!getLifecycleRule().isEmpty()) {
            saveLifecycleRules(client);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        S3Client client = createClient(S3Client.class);

        if (changedFieldNames.contains("tags")) {
            saveTags(client);
        }

        if (changedFieldNames.contains("enable-accelerate-config")) {
            saveAccelerateConfig(client);
        }

        if (changedFieldNames.contains("enable-version")) {
            saveEnableVersion(client);
        }

        if (changedFieldNames.contains("enable-pay")) {
            saveEnablePay(client);
        }

        saveCorsRules(client);

        saveLifecycleRules(client);
    }

    @Override
    public void delete() {
        S3Client client = createClient(S3Client.class);
        client.deleteBucket(
            r -> r.bucket(getName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (!getName().isEmpty()) {
            sb.append(getName());
        } else {
            sb.append("bucket");
        }

        return sb.toString();
    }

    private Bucket getBucket(S3Client client) {
        Bucket bucket = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("Bucket name is missing, unable to load bucket.");
        }

        try {
            ListBucketsResponse listBucketsResponse = client.listBuckets();

            bucket = listBucketsResponse.buckets().stream().filter(o -> o.name().equals(getName())).findFirst().orElse(null);

        } catch (S3Exception ex ) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }

        }

        return bucket;
    }

    private void loadTags(S3Client client) {
        try {
            GetBucketTaggingResponse bucketTagging = client.getBucketTagging(
                r -> r.bucket(getName())
            );

            for (Tag tag : bucketTagging.tagSet()) {
                getTags().put(tag.key(), tag.value());
            }

        } catch (S3Exception s3ex) {
            if (s3ex.awsErrorDetails().errorCode().equals("NoSuchTagSet")) {
                getTags().clear();
            } else {
                throw s3ex;
            }
        }
    }

    private void saveTags(S3Client client) {
        if (getTags().isEmpty()) {
            client.deleteBucketTagging(
                r -> r.bucket(getName())
            );
        } else {
            Set<Tag> tagSet = new HashSet<>();
            for (String key : getTags().keySet()) {
                tagSet.add(Tag.builder().key(key).value(getTags().get(key)).build());
            }

            client.putBucketTagging(
                r -> r.bucket(getName())
                    .tagging(
                        t -> t.tagSet(tagSet)
                            .build()
                    )
            );
        }
    }

    private void loadAccelerateConfig(S3Client client) {
        GetBucketAccelerateConfigurationResponse response = client.getBucketAccelerateConfiguration(
            r -> r.bucket(getName()).build()
        );

        setEnableAccelerateConfig(response.status() != null && response.status().equals(BucketAccelerateStatus.ENABLED));
    }

    private void saveAccelerateConfig(S3Client client) {
        client.putBucketAccelerateConfiguration(
            r -> r.bucket(getName())
                .accelerateConfiguration(
                    ac -> ac.status(getEnableAccelerateConfig() ? BucketAccelerateStatus.ENABLED : BucketAccelerateStatus.SUSPENDED)
                )
        );
    }

    private void loadEnableVersion(S3Client client) {
        GetBucketVersioningResponse response = client.getBucketVersioning(
            r -> r.bucket(getName())
        );
        setEnableVersion(response.status() != null && response.status().equals(BucketVersioningStatus.ENABLED));
    }

    private void saveEnableVersion(S3Client client) {
        client.putBucketVersioning(
            r -> r.bucket(getName())
                .versioningConfiguration(
                    v -> v.status(getEnableVersion() ? BucketVersioningStatus.ENABLED : BucketVersioningStatus.SUSPENDED)
                )
                .build()
        );
    }

    private void loadEnablePay(S3Client client) {
        GetBucketRequestPaymentResponse response = client.getBucketRequestPayment(
            r -> r.bucket(getName()).build()
        );

        setEnablePay(response.payer().equals(Payer.REQUESTER));
    }

    private void saveEnablePay(S3Client client) {
        client.putBucketRequestPayment(
            r -> r.bucket(getName())
                .requestPaymentConfiguration(
                    p -> p.payer(getEnablePay() ? Payer.REQUESTER : Payer.BUCKET_OWNER)
                )
            .build()
        );
    }

    private void loadCorsRules(S3Client client) {
        try {
            GetBucketCorsResponse response = client.getBucketCors(
                r -> r.bucket(getName())
            );

            getCorsRule().clear();
            for (CORSRule corsRule : response.corsRules()) {
                S3CorsRule s3CorsRule = newSubresource(S3CorsRule.class);
                s3CorsRule.copyFrom(corsRule);
                getCorsRule().add(s3CorsRule);
            }
        } catch (S3Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("NoSuchCORSConfiguration")) {
                throw ex;
            }
        }
    }

    private void saveCorsRules(S3Client client) {
        if (getCorsRule().isEmpty()) {
            client.deleteBucketCors(
                r -> r.bucket(getName())
            );
        } else {
            client.putBucketCors(
                r -> r.bucket(getName())
                    .corsConfiguration(c -> c.corsRules(
                        getCorsRule().stream().map(S3CorsRule::toCorsRule).collect(Collectors.toList())
                    ))
            );
        }
    }

    private void loadLifecycleRules(S3Client client) {
        try {
            GetBucketLifecycleConfigurationResponse response = client.getBucketLifecycleConfiguration(
                r -> r.bucket(getName())
            );

            getLifecycleRule().clear();
            for (LifecycleRule lifecycleRule : response.rules()) {
                S3LifecycleRule s3LifecycleRule = newSubresource(S3LifecycleRule.class);
                s3LifecycleRule.copyFrom(lifecycleRule);
                getLifecycleRule().add(s3LifecycleRule);
            }
        } catch (S3Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("NoSuchLifecycleConfiguration")) {
                throw ex;
            }
        }
    }

    private void saveLifecycleRules(S3Client client) {
        if (getLifecycleRule().isEmpty()) {
            client.deleteBucketLifecycle(
                r -> r.bucket(getName())
            );
        } else {
            client.putBucketLifecycleConfiguration(
                r -> r.bucket(getName())
                    .lifecycleConfiguration(
                        l -> l.rules(getLifecycleRule().stream().map(S3LifecycleRule::toLifecycleRule).collect(Collectors.toList()))
                    )
            );
        }
    }
}
