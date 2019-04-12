package gyro.aws.s3;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.diff.Diffable;
import gyro.core.resource.ResourceDiffProperty;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionTransition;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class S3LifecycleRule extends Diffable {
    private String lifecycleRuleName;
    private Integer versionExpirationDays;
    private Boolean expiredObjectDeleteMarker;
    private Integer incompleteMultipartUploadDays;
    private Integer nonCurrentVersionExpirationDays;
    private String status;
    private String prefix;
    private Map<String, String> tags;
    private List<S3LifecycleRuleNonCurrentTransition> nonCurrentTransition;
    private List<S3LifecycleRuleTransition> transition;

    public S3LifecycleRule() {

    }

    public S3LifecycleRule(LifecycleRule lifecycleRule) {
        setLifecycleRuleName(lifecycleRule.id());

        if (lifecycleRule.expiration() != null) {
            setVersionExpirationDays(lifecycleRule.expiration().days());
            setExpiredObjectDeleteMarker(lifecycleRule.expiration().expiredObjectDeleteMarker());
        }

        if (lifecycleRule.abortIncompleteMultipartUpload() != null) {
            setIncompleteMultipartUploadDays(lifecycleRule.abortIncompleteMultipartUpload().daysAfterInitiation());
        }

        if (lifecycleRule.filter() != null) {
            String filterAndPrefix = "";
            String filterPrefix = "";

            Map<String, String> filterAndTag = new HashMap<>();
            Map<String, String> filterTag = new HashMap<>();

            if (lifecycleRule.filter().and() != null) {
                filterAndPrefix = lifecycleRule.filter().and().prefix();
                filterAndTag = fromTags(lifecycleRule.filter().and().tags());
            }

            filterPrefix = lifecycleRule.filter().prefix();

            if (lifecycleRule.filter().tag() != null) {
                filterTag = fromTags(Collections.singletonList(lifecycleRule.filter().tag()));
            }

            if (ObjectUtils.isBlank(filterAndPrefix)) {
                setPrefix(filterPrefix);
            } else {
                setPrefix(filterAndPrefix);
            }

            if (filterTag.isEmpty()) {
                setTags(filterAndTag);
            } else {
                setTags(filterTag);
            }
        }

        if (lifecycleRule.noncurrentVersionExpiration() != null) {
            setNonCurrentVersionExpirationDays(lifecycleRule.noncurrentVersionExpiration().noncurrentDays());
        }

        getNonCurrentTransition().clear();
        if (!lifecycleRule.noncurrentVersionTransitions().isEmpty()) {
            for (NoncurrentVersionTransition noncurrentVersionTransition : lifecycleRule.noncurrentVersionTransitions()) {
                getNonCurrentTransition().add(new S3LifecycleRuleNonCurrentTransition(noncurrentVersionTransition));
            }
        }

        setStatus(lifecycleRule.statusAsString());

        getTransition().clear();
        if (!lifecycleRule.transitions().isEmpty()) {
            for (Transition transition : lifecycleRule.transitions()) {
                getTransition().add(new S3LifecycleRuleTransition(transition));
            }
        }
    }

    /**
     * Name of the life cycle rule. (Required)
     */
    public String getLifecycleRuleName() {
        return lifecycleRuleName;
    }

    public void setLifecycleRuleName(String lifecycleRuleName) {
        this.lifecycleRuleName = lifecycleRuleName;
    }

    /**
     * Current version expiration days. Depends on the values set in transition.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getVersionExpirationDays() {
        return versionExpirationDays;
    }

    public void setVersionExpirationDays(Integer versionExpirationDays) {
        this.versionExpirationDays = versionExpirationDays;
    }

    /**
     * Enable expired object.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getExpiredObjectDeleteMarker() {
        return expiredObjectDeleteMarker;
    }

    public void setExpiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker) {
        this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;
    }

    /**
     * Number of days after which incomplete multipart upload data be deleted.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getIncompleteMultipartUploadDays() {
        return incompleteMultipartUploadDays;
    }

    public void setIncompleteMultipartUploadDays(Integer incompleteMultipartUploadDays) {
        this.incompleteMultipartUploadDays = incompleteMultipartUploadDays;
    }

    /**
     * Non current version expiration days. Depends on the values set in non current transition.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getNonCurrentVersionExpirationDays() {
        return nonCurrentVersionExpirationDays;
    }

    public void setNonCurrentVersionExpirationDays(Integer nonCurrentVersionExpirationDays) {
        this.nonCurrentVersionExpirationDays = nonCurrentVersionExpirationDays;
    }

    /**
     * State of the lifecycle policy. Valid values ``Enabled`` or ``Disabled``. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Apply the rule to objects having this prefix.
     */
    @ResourceDiffProperty(updatable = true)
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Apply the rule to objects having these tags.
     */
    @ResourceDiffProperty(updatable = true)
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
     * Configure the non current transition rules to this lifecycle rule.
     *
     * @subresource gyro.aws.s3.S3LifecycleRuleNonCurrentTransition
     */
    @ResourceDiffProperty(updatable = true)
    public List<S3LifecycleRuleNonCurrentTransition> getNonCurrentTransition() {
        if (nonCurrentTransition == null) {
            nonCurrentTransition = new ArrayList<>();
        }

        return nonCurrentTransition;
    }

    public void setNonCurrentTransition(List<S3LifecycleRuleNonCurrentTransition> nonCurrentTransition) {
        this.nonCurrentTransition = nonCurrentTransition;
    }

    /**
     * Configure the transition rules to this lifecycle rule.
     *
     * @subresource gyro.aws.s3.S3LifecycleRuleTransition
     */
    @ResourceDiffProperty(updatable = true)
    public List<S3LifecycleRuleTransition> getTransition() {
        if (transition == null) {
            transition = new ArrayList<>();
        }


        return transition;
    }

    public void setTransition(List<S3LifecycleRuleTransition> transition) {
        this.transition = transition;
    }

    @Override
    public String primaryKey() {
        return getLifecycleRuleName();
    }

    @Override
    public String toDisplayString() {
        if (ObjectUtils.isBlank(getLifecycleRuleName())) {
            return "lifecycle rule";
        } else {
            return "lifecycle rule - " + getLifecycleRuleName();
        }
    }

    LifecycleRule toLifecycleRule() {
        validateLifecycleRule();

        LifecycleRule.Builder builder = LifecycleRule.builder()
            .id(getLifecycleRuleName())
            .status(getStatus())
            .prefix(null)
            .expiration(
                e -> e.date(null)
                    .days(getVersionExpirationDays())
                    .expiredObjectDeleteMarker(getExpiredObjectDeleteMarker())
            );

        if (getIncompleteMultipartUploadDays() != null) {
            builder = builder.abortIncompleteMultipartUpload(
                a -> a.daysAfterInitiation(getIncompleteMultipartUploadDays())
            );
        }

        if (getTags().isEmpty()) {
            builder = builder.filter(
                f -> f.prefix(ObjectUtils.isBlank(getPrefix()) ? "" : getPrefix())
            );
        } else if (getTags().size() == 1 && ObjectUtils.isBlank(getPrefix())) {
            builder = builder.filter(
                f -> f.prefix(getPrefix())
                    .tag(toTag(getTags()))
            );
        } else {
            builder = builder.filter(
                f -> f.prefix(null).and(
                    l -> l.prefix(getPrefix())
                        .tags(toTags(getTags()))
                    )
            );
        }

        if (getNonCurrentVersionExpirationDays() != null) {
            builder = builder.noncurrentVersionExpiration(
                n -> n.noncurrentDays(getNonCurrentVersionExpirationDays())
            );
        }

        builder = builder.noncurrentVersionTransitions(
            getNonCurrentTransition().stream()
                .map(S3LifecycleRuleNonCurrentTransition::toNoncurrentVersionTransition)
                .collect(Collectors.toList())
        ).transitions(
            getTransition().stream()
                .map(S3LifecycleRuleTransition::toTransition)
                .collect(Collectors.toList())
        );

        return builder.build();
    }

    private Map<String, String> fromTags(List<Tag> tags) {
        Map<String, String> tagMap = new HashMap<>();
        for (Tag tag : tags) {
            tagMap.put(tag.key(), tag.value());
        }
        return tagMap;
    }

    private Tag toTag(Map<String, String> tagMap) {
        for (String key : tagMap.keySet()) {
            return Tag.builder()
                .key(key)
                .value(tagMap.get(key))
                .build();
        }

        return null;
    }

    private List<Tag> toTags(Map<String, String> tagMap) {
        List<Tag> tags = new ArrayList<>();

        for (String key : tagMap.keySet()) {
            tags.add(
                Tag.builder()
                    .key(key)
                    .value(tagMap.get(key))
                    .build()
            );
        }

        return tags;
    }

    private void validateLifecycleRule() {
        if (getExpiredObjectDeleteMarker() != null && getVersionExpirationDays() != null) {
            throw new GyroException("Field: 'expired-object-delete-marker' and Field: 'version-expiration-days' cannot both be set.");
        }

        // When tags present
        if (!getTags().isEmpty()) {
            if (getExpiredObjectDeleteMarker() != null) {
                throw new GyroException("Field: 'expired-object-delete-marker' cannot be set when Field: 'tags' is set.");
            }

            if (getIncompleteMultipartUploadDays() != null) {
                throw new GyroException("Field: 'incomplete-multipart-upload-days' cannot be set when Field: 'tags' is set.");
            }
        }
    }
}
