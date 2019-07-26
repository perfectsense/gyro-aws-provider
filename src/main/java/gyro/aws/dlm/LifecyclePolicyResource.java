package gyro.aws.dlm;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.dlm.DlmClient;
import software.amazon.awssdk.services.dlm.model.CreateLifecyclePolicyResponse;
import software.amazon.awssdk.services.dlm.model.CreateRule;
import software.amazon.awssdk.services.dlm.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.dlm.model.LifecyclePolicy;
import software.amazon.awssdk.services.dlm.model.PolicyDetails;
import software.amazon.awssdk.services.dlm.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dlm.model.RetainRule;
import software.amazon.awssdk.services.dlm.model.Schedule;
import software.amazon.awssdk.services.dlm.model.Tag;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an EBS Snapshot lifecycle policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dlm-lifecycle-policy lifecycle-policy-example
 *         description: "ebs-snapshot-lifecycle-policy-example"
 *         execution-role: "arn:aws:iam::242040583208:role/aws-service-role/autoscaling.amazonaws.com/AWSServiceRoleForAutoScaling"
 *         schedule-name: "ebs-snapshot-lifecycle-policy-example-schedule"
 *         retain-rule-count: 1000
 *         rule-time: "09:00"
 *         rule-interval: 6
 *         tags-to-add: {
 *             addTag: "tag1-val"
 *         }
 *         target-tags: {
 *             targetTag: "tag1-val"
 *         }
 *     end
 */
@Type("dlm-lifecycle-policy")
public class LifecyclePolicyResource extends AwsResource implements Copyable<LifecyclePolicy> {

    private String id;
    private String description;
    private RoleResource executionRole;
    private String resourceType;
    private Map<String, String> targetTags;
    private String state;
    private Date dateCreated;
    private Date dateModified;
    private Boolean copyTags;
    private String scheduleName;
    private Integer ruleInterval;
    private String ruleIntervalUnit;
    private String ruleTime;
    private Integer retainRuleCount;
    private Map<String, String> tagsToAdd;

    /**
     * The description of the snapshot policy. (Required)
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The permission role for the snapshot policy. (Required)0
     */
    @Updatable
    public RoleResource getExecutionRole() {
        return executionRole;
    }

    public void setExecutionRole(RoleResource executionRole) {
        this.executionRole = executionRole;
    }

    /**
     * The resource type of the snapshot policy. Valid values are ``VOLUME`` or ``INSTANCE``. Defaults to ``VOLUME``.
     */
    public String getResourceType() {
        if (resourceType == null) {
            resourceType = "VOLUME";
        }

        return resourceType.toUpperCase();
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * The target tags for the snapshot policy. (Required)
     */
    @Updatable
    public Map<String, String> getTargetTags() {
        if (targetTags == null) {
            targetTags = new HashMap<>();
        }

        return targetTags;
    }

    public void setTargetTags(Map<String, String> targetTags) {
        this.targetTags = targetTags;
    }

    /**
     * The state of the snapshot policy. Valid values are ``ENABLED`` or ``DISABLED``. Defaults to ``ENABLED``
     */
    @Updatable
    public String getState() {
        if (state == null) {
            state = "ENABLED";
        }

        return state.toUpperCase();
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Copy tags to volumes created using this snapshot policy. Defaults to ``false``
     */
    @Updatable
    public Boolean getCopyTags() {
        if (copyTags == null) {
            copyTags = false;
        }

        return copyTags;
    }

    public void setCopyTags(Boolean copyTags) {
        this.copyTags = copyTags;
    }

    /**
     * The name of the schedule for the snapshot policy. (Required)
     */
    @Updatable
    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    /**
     * The name of the schedule for the snapshot policy. Valid values are ``2`` or``3`` or``4`` or``6`` or``8`` or ``12`` or ``24``  (Required)
     */
    @Updatable
    public Integer getRuleInterval() {
        return ruleInterval;
    }

    public void setRuleInterval(Integer ruleInterval) {
        this.ruleInterval = ruleInterval;
    }

    /**
     * The rule interval for the snapshot policy. Valid values are ``HOURS``. Defaults to ``HOURS``
     */
    @Updatable
    public String getRuleIntervalUnit() {
        if (ruleIntervalUnit == null) {
            ruleIntervalUnit = "HOURS";
        }

        return ruleIntervalUnit.toUpperCase();
    }

    public void setRuleIntervalUnit(String ruleIntervalUnit) {
        this.ruleIntervalUnit = ruleIntervalUnit;
    }

    /**
     * The time format of the interval for the snapshot policy. Currenly only supported value is ``hh:mm``. Defaults to ``hh:mm``
     */
    @Updatable
    public String getRuleTime() {
        if (ruleTime == null) {
            ruleTime = "hh:mm";
        }

        return ruleTime;
    }

    public void setRuleTime(String ruleTime) {
        this.ruleTime = ruleTime;
    }

    /**
     * The number of volumes to retain for the snapshot policy. Valid values are ``1`` to ``1000``. (Required)
     */
    @Updatable
    public Integer getRetainRuleCount() {
        return retainRuleCount;
    }

    public void setRetainRuleCount(Integer retainRuleCount) {
        this.retainRuleCount = retainRuleCount;
    }

    /**
     * The list of tags to add to the volumes for the snapshot policy. (Required)
     */
    @Updatable
    public Map<String, String> getTagsToAdd() {
        if (tagsToAdd == null) {
            tagsToAdd = new HashMap<>();
        }

        return tagsToAdd;
    }

    public void setTagsToAdd(Map<String, String> tagsToAdd) {
        this.tagsToAdd = tagsToAdd;
    }

    /**
     * The policy id.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The creation date of the policy.
     */
    @Output
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * The last update date of the policy.
     */
    @Output
    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Override
    public void copyFrom(LifecyclePolicy policy) {
        setDateCreated(Date.from(policy.dateCreated()));
        setDateModified(Date.from(policy.dateModified()));
        setDescription(policy.description());
        setExecutionRole(!ObjectUtils.isBlank(policy.executionRoleArn()) ? findById(RoleResource.class, policy.executionRoleArn()) : null);
        setId(policy.policyId());
        setState(policy.stateAsString());

        PolicyDetails policyDetails = policy.policyDetails();
        setResourceType(policyDetails.resourceTypesAsStrings().get(0));

        if (!policyDetails.schedules().isEmpty()) {
            Schedule schedule = policyDetails.schedules().get(0);
            setCopyTags(schedule.copyTags());
            setScheduleName(schedule.name());

            for (Tag tag : schedule.tagsToAdd()) {
                getTagsToAdd().put(tag.key(), tag.value());
            }

            CreateRule createRule = schedule.createRule();
            if (createRule != null) {
                setRuleInterval(createRule.interval());
                setRuleIntervalUnit(createRule.intervalUnitAsString());
                setRuleTime(createRule.times().get(0));
            }

            RetainRule retainRule = schedule.retainRule();

            if (retainRule != null) {
                setRetainRuleCount(retainRule.count());
            }
        }

        getTargetTags().clear();
        for (Tag tag : policyDetails.targetTags()) {
            getTargetTags().put(tag.key(), tag.value());
        }
    }

    @Override
    public boolean refresh() {
        DlmClient client = createClient(DlmClient.class);

        LifecyclePolicy policy = getPolicy(client);

        if (policy == null) {
            return false;
        }

        copyFrom(policy);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        DlmClient client = createClient(DlmClient.class);

        CreateLifecyclePolicyResponse response = client.createLifecyclePolicy(
            r -> r.description(getDescription())
                .executionRoleArn(getExecutionRole().getArn())
                .policyDetails(
                    pd -> pd.resourceTypesWithStrings(Collections.singletonList(getResourceType()))
                        .schedules(Collections.singleton(getSchedule()))
                        .targetTags(getTags(getTargetTags()))
                )
                .state(getState())
        );

        setId(response.policyId());

        LifecyclePolicy policy = getPolicy(client);
        setDateCreated(Date.from(policy.dateCreated()));
        setDateModified(Date.from(policy.dateModified()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        DlmClient client = createClient(DlmClient.class);

        client.updateLifecyclePolicy(
            r -> r.policyId(getId())
                .description(getDescription())
                .executionRoleArn(getExecutionRole().getArn())
                .policyDetails(
                    pd -> pd.resourceTypesWithStrings(Collections.singletonList(getResourceType()))
                        .schedules(Collections.singleton(getSchedule()))
                        .targetTags(getTags(getTargetTags()))
                )
                .state(getState())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        DlmClient client = createClient(DlmClient.class);

        client.deleteLifecyclePolicy(
            r -> r.policyId(getId())
        );
    }

    private Schedule getSchedule() {
        return Schedule.builder()
            .tagsToAdd(getTags(getTagsToAdd()))
            .name(getScheduleName())
            .retainRule(r -> r.count(getRetainRuleCount()))
            .copyTags(getCopyTags())
            .createRule(
                cr -> cr.times(Collections.singletonList(getRuleTime()))
                    .interval(getRuleInterval())
                    .intervalUnit(getRuleIntervalUnit())
            )
            .build();
    }

    private List<Tag> getTags(Map<String, String> tagMap) {
        return tagMap.entrySet().stream()
            .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
            .collect(Collectors.toList());
    }

    private LifecyclePolicy getPolicy(DlmClient client) {
        LifecyclePolicy lifecyclePolicy = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load ebs snapshot policy.");
        }

        try {
            GetLifecyclePolicyResponse response = client.getLifecyclePolicy(
                r -> r.policyId(getId())
            );

            lifecyclePolicy = response.policy();
        } catch (ResourceNotFoundException ignore) {
            //ignore
        }

        return lifecyclePolicy;
    }
}
