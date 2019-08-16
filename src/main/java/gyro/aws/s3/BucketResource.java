package gyro.aws.s3;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.CompactMap;
import gyro.core.scope.State;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketAccelerateStatus;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketRequestPaymentResponse;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.Payer;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.BucketLoggingStatus;
import software.amazon.awssdk.services.s3.model.GetBucketReplicationResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
 *         tags: {
 *             Name: "bucket-example"
 *         }
 *         enable-accelerate-config: true
 *         enable-versioning: true
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
 *         tags: {
 *             Name: "bucket-example"
 *         }
 *         enable-accelerate-config: true
 *         enable-versioning: true
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
 *         tags: {
 *             Name: "bucket-example"
 *         }
 *         enable-accelerate-config: true
 *         enable-versioning: true
 *
 *         lifecycle-rule
 *             id: "rule no prefix and no tag"
 *             status: "Disabled"
 *
 *             transition
 *                 days: 40
 *                 storage-class: "STANDARD_IA"
 *             end
 *
 *             noncurrent-version-transition
 *                 days: 40
 *                 storage-class: "STANDARD_IA"
 *             end
 *
 *             expiration
 *                 expired-object-delete-marker: false
 *             end
 *
 *             noncurrent-version-expiration
 *                 days: 403
 *             end
 *
 *             abort-incomplete-multipart-upload
 *                 days-after-initiation: 5
 *             end
 *         end
 *     end
 *
 * Example with replication configuration
 * -------
 * .. code-block:: gyro
 *
 *     aws::s3-bucket bucket-example
 *         name: "beam-sandbox-bucket-us-east-2"
 *         tags: {
 *             Name: "bucket-example",
 *             Name2: "something"
 *         }
 *         enable-accelerate-config: true
 *         enable-versioning: true
 *
 *         replication-configuration
 *             role: "arn:aws:iam::242040583208:role/service-role/s3crr_role_for_sandbox-bucket-example-logging_to_beam-sandbox-br"
 *             rule
 *                 id: "example_to_east_1"
 *                 destination
 *                     bucket: "beam-sandbox-ops-us-east-1a"
 *                 end
 *                 filter
 *                     prefix: "logs/"
 *                 end
 *                 priority: 1
 *                 status: enabled
 *                 delete-marker-replication-status: disabled
 *             end
 *
 *             rule
 *                 id: "another_example_to_east_1"
 *                 destination
 *                     bucket: "beam-sandbox-ops-us-east-1a"
 *                 end
 *                 filter
 *                     and-operator
 *                         prefix: "thousand-year-door"
 *                         tag
 *                             key: "paper"
 *                             value: "mario"
 *                         end
 *                     end
 *                 end
 *                 priority: 2
 *                 status: enabled
 *                 delete-marker-replication-status: disabled
 *             end
 *         end
 *     end
 *
 *
 * Example with logging enabled
 * -------
 * .. code-block:: gyro
 *
 *     aws::s3-bucket bucket-example
 *         name: "beam-sandbox-logging-enabled"
 *         enable-object-lock: false
 *         tags: {
 *             Name: "bucket-example",
 *             Name2: "something"
 *         }
 *
 *         logging
 *             bucket: "beam-sandbox-s3-logs"
 *         end
 *
 *         enable-accelerate-config: true
 *         enable-versioning: true
 *     end
 */
@Type("s3-bucket")
public class BucketResource extends AwsResource implements Copyable<Bucket> {

    private String name;
    private Boolean enableObjectLock;
    private Map<String, String> tags;
    private Boolean enableAccelerateConfig;
    private Boolean enableVersioning;
    private String requestPayer;
    private List<S3CorsRule> corsRule;
    private List<S3LifecycleRule> lifecycleRule;
    private String domainName;
    private S3LoggingEnabled logging;
    private S3ReplicationConfiguration replicationConfiguration;

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
    public Boolean getEnableVersioning() {
        if (enableVersioning == null) {
            enableVersioning = false;
        }

        return enableVersioning;
    }

    public void setEnableVersioning(Boolean enableVersioning) {
        this.enableVersioning = enableVersioning;
    }

    /**
     * Does the requester pay for requests to the bucket or the owner. Defaults to ``BUCKET_OWNER``. Valid values are ``BUCKET_OWNER`` or ``REQUESTER``. See `S3 Requester Pays Bucket <https://docs.aws.amazon.com/AmazonS3/latest/dev/RequesterPaysBuckets.html/>`_.
     */
    @Updatable
    public String getRequestPayer() {
        if (requestPayer == null) {
            requestPayer = "BUCKET_OWNER";
        }

        return requestPayer.toUpperCase();
    }

    public void setRequestPayer(String requestPayer) {
        this.requestPayer = requestPayer;

        try {
            Payer.valueOf(requestPayer.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new GyroException("Invalid value for param 'request-payer'. Valid values are 'BUCKET_OWNER' or 'REQUESTER'.");
        }
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

    /**
     * Configure where access logs are sent.
     *
     * @subresource gyro.aws.s3.S3LoggingEnabled
     */
    @Updatable
    public S3LoggingEnabled getLogging() {
        return logging;
    }

    public void setLogging(S3LoggingEnabled logging) {
        this.logging= logging;
    }

    /**
     * Configure the replication rules for the bucket.
     *
     * @subresource gyro.aws.s3.S3ReplicationConfiguration
     */
    @Updatable
    public S3ReplicationConfiguration getReplicationConfiguration() {
        return replicationConfiguration;
    }

    public void setReplicationConfiguration(S3ReplicationConfiguration replicationConfiguration) {
        this.replicationConfiguration = replicationConfiguration;
    }

    @Override
    public void copyFrom(Bucket bucket) {
        S3Client client = createClient(S3Client.class);
        setName(bucket.name());
        loadTags(client);
        loadAccelerateConfig(client);
        loadEnableVersioning(client);
        loadRequestPayer(client);
        loadCorsRules(client);
        loadLifecycleRules(client);
        loadBucketLogging(client);
        loadReplicationConfiguration(client);
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
    public void create(GyroUI ui, State state) {
        S3Client client = createClient(S3Client.class);

        client.createBucket(
            r -> r.bucket(getName())
                .objectLockEnabledForBucket(getEnableObjectLock())
        );
        state.save();

        if (!getTags().isEmpty()) {
            saveTags(client);
        }

        if (getEnableAccelerateConfig()) {
            saveAccelerateConfig(client);
        }

        if (getEnableVersioning()) {
            saveEnableVersioning(client);
        }

        if (getRequestPayer().equalsIgnoreCase(Payer.REQUESTER.name())) {
            saveRequestPayer(client);
        }

        if (!getCorsRule().isEmpty()) {
            saveCorsRules(client);
        }

        if (!getLifecycleRule().isEmpty()) {
            saveLifecycleRules(client);
        }

        if (getReplicationConfiguration() != null) {
            saveReplicationConfiguration(client);
        }

        if (getLogging() != null) {
            saveBucketLogging(client);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        S3Client client = createClient(S3Client.class);

        if (changedFieldNames.contains("tags")) {
            saveTags(client);
        }

        if (changedFieldNames.contains("enable-accelerate-config")) {
            saveAccelerateConfig(client);
        }

        if (changedFieldNames.contains("enable-versioning")) {
            saveEnableVersioning(client);
        }

        if (changedFieldNames.contains("request-payer")) {
            saveRequestPayer(client);
        }

        if (changedFieldNames.contains("logging")) {
            saveBucketLogging(client);
        }

        saveReplicationConfiguration(client);

        saveCorsRules(client);

        saveLifecycleRules(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        S3Client client = createClient(S3Client.class);
        client.deleteBucket(
            r -> r.bucket(getName())
        );
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

    private void loadEnableVersioning(S3Client client) {
        GetBucketVersioningResponse response = client.getBucketVersioning(
            r -> r.bucket(getName())
        );
        setEnableVersioning(response.status() != null && response.status().equals(BucketVersioningStatus.ENABLED));
    }

    private void saveEnableVersioning(S3Client client) {
        client.putBucketVersioning(
            r -> r.bucket(getName())
                .versioningConfiguration(
                    v -> v.status(getEnableVersioning() ? BucketVersioningStatus.ENABLED : BucketVersioningStatus.SUSPENDED)
                )
                .build()
        );
    }

    private void loadRequestPayer(S3Client client) {
        GetBucketRequestPaymentResponse response = client.getBucketRequestPayment(
            r -> r.bucket(getName()).build()
        );

        setRequestPayer(response.payer().name());
    }

    private void saveRequestPayer(S3Client client) {
        client.putBucketRequestPayment(
            r -> r.bucket(getName())
                .requestPaymentConfiguration(
                    p -> p.payer(getRequestPayer())
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

            Wait.atMost(2, TimeUnit.MINUTES)
                .prompt(false)
                .checkEvery(10, TimeUnit.SECONDS)
                .until(() -> isCorsSaved(client));
        }
    }

    private boolean isCorsSaved(S3Client client) {
        BucketResource bucketResource = new BucketResource();
        bucketResource.setName(getName());
        bucketResource.loadCorsRules(client);

        Set<String> currentCors = bucketResource.getCorsRule().stream().map(S3CorsRule::primaryKey).collect(Collectors.toSet());
        return getCorsRule().stream().allMatch(o -> currentCors.contains(o.primaryKey()));
    }

    private void loadBucketLogging(S3Client client) {
        GetBucketLoggingResponse response = client.getBucketLogging(
                r -> r.bucket(getName()).build()
        );

        if (response.loggingEnabled() != null) {
            setLogging(newSubresource(S3LoggingEnabled.class));
            getLogging().copyFrom(response.loggingEnabled());
        } else {
            setLogging(null);
        }
    }

    private void saveBucketLogging(S3Client client){
        if (getLogging() != null) {
            client.putBucketLogging(
                r -> r.bucket(getName())
                    .bucketLoggingStatus(s -> s.loggingEnabled(
                        getLogging().toLoggingEnabled()
                    ))
            );
        } else {
            client.putBucketLogging(
                r -> r.bucket(getName())
                    .bucketLoggingStatus(BucketLoggingStatus.builder().build())
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

    private void loadReplicationConfiguration(S3Client client) {
        try {
            GetBucketReplicationResponse response = client.getBucketReplication(
                r -> r.bucket(getName())
            );

            setReplicationConfiguration(newSubresource(S3ReplicationConfiguration.class));
            getReplicationConfiguration().copyFrom(response.replicationConfiguration());

        } catch (S3Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("ReplicationConfigurationNotFoundError")) {
                throw ex;
            } else {
                setReplicationConfiguration(null);
            }
        }
    }

    private void saveReplicationConfiguration(S3Client client) {
        if (getReplicationConfiguration() == null || getReplicationConfiguration().getRule().isEmpty()) {
            client.deleteBucketReplication(
                    r -> r.bucket(getName())
            );
        } else {
            client.putBucketReplication(
                    r -> r.bucket(getName())
                            .replicationConfiguration(getReplicationConfiguration().toReplicationConfiguration())
            );
        }
    }

    public String getDomainName() {
        S3Client client = createClient(S3Client.class);
        return String.format("%s.s3.%s.amazonaws.com", getName(), getBucketRegion(client));
    }

    private String getBucketRegion(S3Client client) {
        try {
            GetBucketLocationResponse response = client.getBucketLocation(r -> r.bucket(getName()));
            return ObjectUtils.isBlank(response.locationConstraintAsString()) ? "us-east-1" : response.locationConstraintAsString();
        } catch (NoSuchBucketException ex) {
            throw new GyroException(String.format("Bucket %s was not found.", getName()));
        } catch (S3Exception exx) {
            if (exx.awsErrorDetails().errorCode().equalsIgnoreCase("AccessDenied")) {
                throw new GyroException(String.format("You don't have access to Bucket %s.", getName()));
            } else {
                throw exx;
            }
        }
    }
}