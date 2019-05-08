package gyro.aws.cloudwatch;

import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest;

import java.util.Set;

public class CloudWatchRuleTargetResource extends AwsResource {

    private String targetId;
    private String targetArn;
    private String roleArn;
    private String messageGroupId;

    public CloudWatchRuleTargetResource() {
    }

    public CloudWatchRuleTargetResource(Target target) {
        setTargetId(target.id());
        setTargetArn(target.arn());
        setRoleArn(target.roleArn());
        setMessageGroupId(target.sqsParameters().messageGroupId());
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    @ResourceUpdatable
    public String getTargetArn() {
        return targetArn;
    }

    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    @ResourceUpdatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    @ResourceUpdatable
    public String getMessageGroupId() {
        return messageGroupId;
    }

    public void setMessageGroupId(String messageGroupId) {
        this.messageGroupId = messageGroupId;
    }

    public String getRuleName() {
        CloudWatchEventRuleResource ruleResource = (CloudWatchEventRuleResource) parent();

        if (ruleResource != null) {
            return ruleResource.getRuleName();
        }
        return null;
    }

    @Override
    public String primaryKey() {
        return String.format("%s, %s", getTargetId(), getTargetArn());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveTarget(client);

    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveTarget(client);

    }

    @Override
    public void delete() {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        client.removeTargets(r -> r.force(true).rule(getRuleName()).ids(getTargetId()));
    }

    @Override
    public String toDisplayString() {

        StringBuilder sb = new StringBuilder();
        sb.append("target");

        if (getTargetId() != null) {
            sb.append(" ").append(getTargetId());
        }
        return sb.toString();

    }

    private void saveTarget(CloudWatchEventsClient client) {
        Target target = Target.builder()
                .arn(getTargetArn())
                .id(getTargetId())
                .roleArn(getRoleArn())
                .sqsParameters(
                        g -> g.messageGroupId(getMessageGroupId())
                )
                .build();

        PutTargetsRequest request = PutTargetsRequest.builder()
                .rule(getRuleName())
                .targets(target)
                .build();

        client.putTargets(request);

    }

}

