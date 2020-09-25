/*
 * Copyright 2020, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.cloudtrail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.aws.s3.BucketResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.CreateTrailResponse;
import software.amazon.awssdk.services.cloudtrail.model.EventSelector;
import software.amazon.awssdk.services.cloudtrail.model.GetTrailStatusResponse;
import software.amazon.awssdk.services.cloudtrail.model.InsightNotEnabledException;
import software.amazon.awssdk.services.cloudtrail.model.ResourceTag;
import software.amazon.awssdk.services.cloudtrail.model.Tag;
import software.amazon.awssdk.services.cloudtrail.model.Trail;
import software.amazon.awssdk.services.cloudtrail.model.UpdateTrailRequest;

/**
 * Creates a Cloud Trail.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cloudtrail example-cloudtrail
 *         name: "example-cloudtrail"
 *         bucket: $(external-query aws::s3-bucket { name: "example-gyro-trail-bucket" })
 *
 *         event-selector
 *             read-write-type: "All"
 *             include-management-events: true
 *             data-resource
 *                 type: 'AWS::S3::Object'
 *                 values: ['arn:aws:s3:::example-gyro-trail-bucket/']
 *             end
 *             data-resource
 *                 type: 'AWS::Lambda::Function'
 *                 values: ['arn:aws:lambda']
 *             end
 *             management-event-sources-to-exclude: ["kms.amazonaws.com"]
 *         end
 *
 *         insight-selector
 *             insight-type: "ApiCallRateInsight"
 *         end
 *
 *         tags: {
 *             "example-key": "example-value"
 *         }
 *     end
 */
@Type("cloudtrail")
public class CloudTrailResource extends AwsResource implements Copyable<Trail> {

    private String name;
    private BucketResource bucket;
    private String bucketKeyPrefix;
    private String snsTopicName;
    private Boolean includeGlobalServiceEvents;
    private Boolean isMultiRegionTrail;
    private Boolean enableLogFileValidation;
    private String logGroupArn;
    private RoleResource logsRole;
    private KmsKeyResource key;
    private Boolean isOrganizationTrail;
    private CloudTrailEventSelector eventSelector;
    private List<CloudTrailInsightSelector> insightSelector;
    private Map<String, String> tags;
    private Boolean enableLogging;

    // Read-only
    private String arn;
    private Date latestCloudWatchLogsDeliveryTime;
    private Date latestS3DeliveryTime;
    private Date latestDigestDeliveryTime;
    private Date startLoggingTime;
    private Date stopLoggingTime;

    /**
     * The name of the trail.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Amazon S3 bucket designated for publishing log files.
     */
    @Required
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * The Amazon S3 key prefix that comes after the name of the bucket you have designated for log file delivery.
     */
    @Updatable
    public String getBucketKeyPrefix() {
        return bucketKeyPrefix;
    }

    public void setBucketKeyPrefix(String bucketKeyPrefix) {
        this.bucketKeyPrefix = bucketKeyPrefix;
    }

    /**
     * The Amazon SNS topic defined for notification of log file delivery.
     */
    @Updatable
    public String getSnsTopicName() {
        return snsTopicName;
    }

    public void setSnsTopicName(String snsTopicName) {
        this.snsTopicName = snsTopicName;
    }

    /**
     * Option to publish events from global services.
     */
    @Updatable
    public Boolean getIncludeGlobalServiceEvents() {
        return includeGlobalServiceEvents;
    }

    public void setIncludeGlobalServiceEvents(Boolean includeGlobalServiceEvents) {
        this.includeGlobalServiceEvents = includeGlobalServiceEvents;
    }

    /**
     * Option to specify if the trail is created in the current region or in all regions.
     */
    @Updatable
    public Boolean getIsMultiRegionTrail() {
        return isMultiRegionTrail;
    }

    public void setIsMultiRegionTrail(Boolean multiRegionTrail) {
        isMultiRegionTrail = multiRegionTrail;
    }

    /**
     * Option to enable log file validation.
     */
    @Updatable
    public Boolean getEnableLogFileValidation() {
        return enableLogFileValidation;
    }

    public void setEnableLogFileValidation(Boolean enableLogFileValidation) {
        this.enableLogFileValidation = enableLogFileValidation;
    }

    /**
     * The log group to which CloudTrail logs will be delivered.
     */
    @Updatable
    @DependsOn("logs-role")
    public String getLogGroupArn() {
        return logGroupArn;
    }

    public void setLogGroupArn(String logGroupArn) {
        this.logGroupArn = logGroupArn;
    }

    /**
     * The role for the CloudWatch Logs endpoint to assume to write to a user's log group.
     */
    @Updatable
    public RoleResource getLogsRole() {
        return logsRole;
    }

    public void setLogsRole(RoleResource logsRole) {
        this.logsRole = logsRole;
    }

    /**
     * The KMS key to use to encrypt the logs delivered by CloudTrail.
     */
    @Updatable
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    /**
     * Option to specify if the trail is is created for all accounts in an organization or the current AWS account.
     */
    @Updatable
    public Boolean getIsOrganizationTrail() {
        return isOrganizationTrail;
    }

    public void setIsOrganizationTrail(Boolean organizationTrail) {
        isOrganizationTrail = organizationTrail;
    }

    /**
     * The list of tags.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The list of management and data event settings for the trail.
     *
     * @subresource gyro.aws.cloudtrail.CloudTrailEventSelector
     */
    @Updatable
    public CloudTrailEventSelector getEventSelector() {
        return eventSelector;
    }

    public void setEventSelector(CloudTrailEventSelector eventSelector) {
        this.eventSelector = eventSelector;
    }

    /**
     * The list of insight types that are logged on the trail.
     */
    @Updatable
    public List<CloudTrailInsightSelector> getInsightSelector() {
        if (insightSelector == null) {
            insightSelector = new ArrayList<>();
        }

        return insightSelector;
    }

    public void setInsightSelector(List<CloudTrailInsightSelector> insightSelector) {
        this.insightSelector = insightSelector;
    }

    /**
     * Enable the recording of AWS API calls and log file delivery for a trail. Defaults to ``false``.
     */
    @Updatable
    public Boolean getEnableLogging() {
        if (enableLogging == null) {
            enableLogging = Boolean.FALSE;
        }

        return enableLogging;
    }

    public void setEnableLogging(Boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    /**
     * The Amazon Resource Number of the trail.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The most recent date and time when CloudTrail delivered logs to CloudWatch Logs.
     */
    @Output
    public Date getLatestCloudWatchLogsDeliveryTime() {
        return latestCloudWatchLogsDeliveryTime;
    }

    public void setLatestCloudWatchLogsDeliveryTime(Date latestCloudWatchLogsDeliveryTime) {
        this.latestCloudWatchLogsDeliveryTime = latestCloudWatchLogsDeliveryTime;
    }

    /**
     * The most recent date and time when CloudTrail delivered logs to the S3 bucket.
     */
    @Output
    public Date getLatestS3DeliveryTime() {
        return latestS3DeliveryTime;
    }

    public void setLatestS3DeliveryTime(Date latestS3DeliveryTime) {
        this.latestS3DeliveryTime = latestS3DeliveryTime;
    }

    /**
     * The most recent date and time when CloudTrail delivered a digest file to the S3 bucket.
     */
    @Output
    public Date getLatestDigestDeliveryTime() {
        return latestDigestDeliveryTime;
    }

    public void setLatestDigestDeliveryTime(Date latestDigestDeliveryTime) {
        this.latestDigestDeliveryTime = latestDigestDeliveryTime;
    }

    /**
     * The most recent date and time when CloudTrail started recording API calls for an AWS account.
     */
    @Output
    public Date getStartLoggingTime() {
        return startLoggingTime;
    }

    public void setStartLoggingTime(Date startLoggingTime) {
        this.startLoggingTime = startLoggingTime;
    }

    /**
     * The most recent date and time when CloudTrail stopped recording API calls for an AWS account.
     */
    @Output
    public Date getStopLoggingTime() {
        return stopLoggingTime;
    }

    public void setStopLoggingTime(Date stopLoggingTime) {
        this.stopLoggingTime = stopLoggingTime;
    }

    @Override
    public void copyFrom(Trail model) {
        CloudTrailClient client = createClient(CloudTrailClient.class);

        setName(model.name());
        setBucket(findById(BucketResource.class, model.s3BucketName()));
        setBucketKeyPrefix(model.s3KeyPrefix());
        setSnsTopicName(model.snsTopicName());
        setIncludeGlobalServiceEvents(model.includeGlobalServiceEvents());
        setIsMultiRegionTrail(model.isMultiRegionTrail());
        setEnableLogFileValidation(model.logFileValidationEnabled());
        setLogGroupArn(model.cloudWatchLogsLogGroupArn());
        setLogsRole(findById(RoleResource.class, model.cloudWatchLogsRoleArn()));
        setKey(findById(KmsKeyResource.class, model.kmsKeyId()));
        setIsOrganizationTrail(model.isOrganizationTrail());
        setArn(model.trailARN());

        GetTrailStatusResponse trailStatus = client.getTrailStatus(r -> r.name(getName()));
        setEnableLogging(trailStatus.isLogging());
        setLatestCloudWatchLogsDeliveryTime(trailStatus.latestCloudWatchLogsDeliveryTime() != null ?
                Date.from(trailStatus.latestCloudWatchLogsDeliveryTime()) : null);
        setLatestS3DeliveryTime(trailStatus.latestDeliveryTime() != null ?
                Date.from(trailStatus.latestDeliveryTime()) : null);
        setLatestDigestDeliveryTime(trailStatus.latestDigestDeliveryTime() != null ?
                Date.from(trailStatus.latestDigestDeliveryTime()) : null);
        setStartLoggingTime(trailStatus.startLoggingTime() != null ? Date.from(trailStatus.startLoggingTime()) : null);
        setStopLoggingTime(trailStatus.stopLoggingTime() != null ? Date.from(trailStatus.stopLoggingTime()) : null);

        List<EventSelector> eventSelectors = client.getEventSelectors(r -> r.trailName(getName())).eventSelectors().stream()
                .filter(r -> !r.dataResources().isEmpty()).collect(Collectors.toList());
        if (!eventSelectors.isEmpty()) {
            CloudTrailEventSelector cloudTrailEventSelector = newSubresource(CloudTrailEventSelector.class);
            cloudTrailEventSelector.copyFrom(eventSelectors.get(0));
            setEventSelector(cloudTrailEventSelector);
        }

        try {
            setInsightSelector(client.getInsightSelectors(r -> r.trailName(getName())).insightSelectors().stream().map(r -> {
                CloudTrailInsightSelector cloudTrailInsightSelector = newSubresource(CloudTrailInsightSelector.class);
                cloudTrailInsightSelector.copyFrom(r);
                return cloudTrailInsightSelector;
            }).collect(Collectors.toList()));
        } catch (InsightNotEnabledException ex) {

        }

        ResourceTag resourceTag = client.listTags(r -> r.resourceIdList(getArn())).resourceTagList().get(0);
        if (resourceTag.hasTagsList()) {
            resourceTag.tagsList().forEach(t -> getTags().put(t.key(), t.value()));
        }
    }

    @Override
    public boolean refresh() {
        CloudTrailClient client = createClient(CloudTrailClient.class);

        Trail trail = client.getTrail(r -> r.name(getName())).trail();

        if (trail == null) {
            return false;
        }

        copyFrom(trail);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudTrailClient client = createClient(CloudTrailClient.class);

        CreateTrailResponse trail = client.createTrail(r -> r.name(getName())
                .s3BucketName(getBucket().getName())
                .s3KeyPrefix(getBucketKeyPrefix() != null ? getBucketKeyPrefix() : "")
                .snsTopicName(getSnsTopicName())
                .includeGlobalServiceEvents(getIncludeGlobalServiceEvents())
                .isMultiRegionTrail(getIsMultiRegionTrail())
                .enableLogFileValidation(getEnableLogFileValidation())
                .cloudWatchLogsLogGroupArn(getLogGroupArn())
                .cloudWatchLogsRoleArn(getLogsRole() != null ? getLogsRole().getArn() : null)
                .kmsKeyId(getKey() != null ? getKey().getArn() : null)
                .isOrganizationTrail(getIsOrganizationTrail()));

        setArn(trail.trailARN());
        state.save();

        if (getEventSelector() != null) {
            manageEventSelectors(client);
            state.save();
        }

        if (!getInsightSelector().isEmpty()) {
            manageInsightSelectors(client);
            state.save();
        }
        if (getEnableLogging()) {
            manageLogging(client);
        }

        if (!getTags().isEmpty()) {
            client.addTags(r -> r.resourceId(getArn()).tagsList(getTags().entrySet().stream().map(e -> Tag.builder().key(e.getKey())
                    .value(e.getValue()).build()).collect(Collectors.toList())));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudTrailClient client = createClient(CloudTrailClient.class);

        UpdateTrailRequest.Builder builder = UpdateTrailRequest.builder().name(getName());

        if (changedFieldNames.contains("bucket")) {
            builder = builder.s3BucketName(getBucket().getName());
        }

        if (changedFieldNames.contains("bucket-key-prefix")) {
            builder = builder.s3KeyPrefix(getBucketKeyPrefix() != null ? getBucketKeyPrefix() : "");
        }

        if (changedFieldNames.contains("sns-topic-name")) {
            builder = builder.snsTopicName(getSnsTopicName());
        }

        if (changedFieldNames.contains("include-global-service-events")) {
            builder = builder.includeGlobalServiceEvents(getIncludeGlobalServiceEvents());
        }

        if (changedFieldNames.contains("is-multi-region-trail")) {
            builder = builder.isMultiRegionTrail(getIsMultiRegionTrail());
        }

        if (changedFieldNames.contains("enable-log-file-validation")) {
            builder = builder.enableLogFileValidation(getEnableLogFileValidation());
        }

        if (changedFieldNames.contains("log-group-arn")) {
            builder = builder.cloudWatchLogsLogGroupArn(getLogGroupArn());
        }

        if (changedFieldNames.contains("is-organization-trail")) {
            builder = builder.isOrganizationTrail(getIsOrganizationTrail());
        }

        if (changedFieldNames.contains("logs-role")) {
            builder = builder.cloudWatchLogsRoleArn(getLogsRole() != null ? getLogsRole().getArn() : null);
        }

        if (changedFieldNames.contains("key")) {
            builder = builder.kmsKeyId(getKey() != null ? getKey().getArn() : null);
        }

        client.updateTrail(builder.build());

        if (changedFieldNames.contains("insight-selector")) {
            manageInsightSelectors(client);
        }

        if (changedFieldNames.contains("event-selector")) {
            manageEventSelectors(client);
        }

        if (changedFieldNames.contains("enable-logging")) {
            manageLogging(client);
        }

        if (changedFieldNames.contains("tags")) {
            CloudTrailResource currentResource = (CloudTrailResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.removeTags(r -> r.resourceId(getArn())
                        .tagsList(currentResource.getTags().entrySet().stream()
                                .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                                .collect(Collectors.toList())));
            }

            if (!getTags().isEmpty()) {
                client.addTags(r -> r.resourceId(getArn()).tagsList(getTags().entrySet().stream()
                        .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                        .collect(Collectors.toList())));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudTrailClient client = createClient(CloudTrailClient.class);

        client.deleteTrail(r -> r.name(getName()));
    }

    private void manageInsightSelectors(CloudTrailClient client) {
        client.putInsightSelectors(r -> r.trailName(getName())
                .insightSelectors(getInsightSelector()
                        .stream()
                        .map(CloudTrailInsightSelector::toInsightSelector)
                        .collect(Collectors.toList())));
    }

    private void manageEventSelectors(CloudTrailClient client) {
        client.putEventSelectors(r -> r.trailName(getName()).eventSelectors(getEventSelector().toEventSelector()));
    }

    private void manageLogging(CloudTrailClient client) {
        if (getEnableLogging()) {
            client.startLogging(r -> r.name(getName()));

        } else {
            client.stopLogging(r -> r.name(getName()));
        }
    }
}
