package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RuleTargetResource extends AwsResource implements Copyable<Target> {

    private String targetId;
    private String targetArn;
    private RoleResource role;
    private String messageGroupId;
    private Integer ecsTaskCount;
    private String ecsTaskDefinitionArn;
    private String input;
    private String inputPath;
    private Map<String, String> inputTransformerPathMap;
    private String inputTransformerTemplate;
    private String kinesisPartitionKeyPath;

    /**
     * The identifier of the target resource. (Required)
     */
    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * The arn of the target resource. (Required)
     */
    @Updatable
    public String getTargetArn() {
        return targetArn;
    }

    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    /**
     * The IAM role arn that gives permission to invoke actions on the target resource. (Optional)
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The input json text that triggers an action on the target resource. If provided, doesn't consider the matched event conditions. (Optional)
     */
    @Updatable
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    /**
     * The input path that passes the part of the event to trigger the target resource. (Optional)
     */
    @Updatable
    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    /**
     * The SQS queue message group id which will be the destination for the target when triggered. The SQS queue must be a FIFO type and should have content-based-deduplication enabled.
     */
    @Updatable
    public String getMessageGroupId() {
        return messageGroupId;
    }

    public void setMessageGroupId(String messageGroupId) {
        this.messageGroupId = messageGroupId;
    }

    /**
     * The number of tasks created when the target is an Amazon ECS task.
     */
    @Updatable
    public Integer getEcsTaskCount() {
        return ecsTaskCount;
    }

    public void setEcsTaskCount(Integer ecsTaskCount) {
        this.ecsTaskCount = ecsTaskCount;
    }

    /**
     * The arn of the task that is created when the target is an Amazon ECS task.
     */
    @Updatable
    public String getEcsTaskDefinitionArn() {
        return ecsTaskDefinitionArn;
    }

    public void setEcsTaskDefinitionArn(String ecsTaskDefinitionArn) {
        this.ecsTaskDefinitionArn = ecsTaskDefinitionArn;
    }

    /**
     * The map of customized inputs that can be used to configure cloudwatch event targets. See `AWS Services CloudWatch InputTransformer property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-inputtransformer.html/>`_.
     */
    @Updatable
    public Map<String, String> getInputTransformerPathMap() {
        if (inputTransformerPathMap == null) {
            inputTransformerPathMap = new HashMap<>();
        }
        return inputTransformerPathMap;
    }

    public void setInputTransformerPathMap(Map<String, String> inputTransformerPathMap) {
        this.inputTransformerPathMap = inputTransformerPathMap;
    }

    /**
     * The input template which contains the values of the keys of the input path maps. See `AWS Services CloudWatch InputTransformer property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-inputtransformer.html/>`_.
     */
    @Updatable
    public String getInputTransformerTemplate() {
        return inputTransformerTemplate;
    }

    public void setInputTransformerTemplate(String inputTransformerTemplate) {
        this.inputTransformerTemplate = inputTransformerTemplate;
    }

    /**
     * The path of the kinesis data stream target. See `AWS Services CloudWatch Kinesis parameter property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-kinesisparameters.html/>`_.
     *
     */
    @Updatable
    public String getKinesisPartitionKeyPath() {
        return kinesisPartitionKeyPath;
    }

    public void setKinesisPartitionKeyPath(String kinesisPartitionKeyPath) {
        this.kinesisPartitionKeyPath = kinesisPartitionKeyPath;
    }

    @Override
    public String primaryKey() {
        return getTargetId();
    }

    @Override
    public void copyFrom(Target target) {
        setTargetId(target.id());
        setTargetArn(target.arn());
        setRole(!ObjectUtils.isBlank(target.roleArn()) ? findById(RoleResource.class, target.roleArn()) : null);
        setMessageGroupId(target.sqsParameters() != null ? target.sqsParameters().messageGroupId() : null);
        setInput(target.input());
        setInputPath(target.inputPath());

        setKinesisPartitionKeyPath(target.kinesisParameters() != null ? target.kinesisParameters().partitionKeyPath() : null);

        if (target.ecsParameters() != null) {
            setEcsTaskCount(target.ecsParameters().taskCount());
            setEcsTaskDefinitionArn(target.ecsParameters().taskDefinitionArn());
        }

        if (target.inputTransformer() != null) {
            setInputTransformerTemplate(target.inputTransformer().inputTemplate());
            Set<Map.Entry<String, String>> entrySet = target.inputTransformer().inputPathsMap().entrySet();

            for (Map.Entry<String, String> entry: entrySet) {
                getInputTransformerPathMap().put(entry.getKey(),entry.getValue());
            }
        }
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(State state) {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveTarget(client);
    }

    @Override
    public void update(State state, Resource current, Set<String> changedProperties) {
        CloudWatchEventsClient client = createClient(CloudWatchEventsClient.class);

        saveTarget(client);
    }

    @Override
    public void delete(State state) {
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

    private String getRuleName() {
        EventRuleResource ruleResource = (EventRuleResource) parent();

        if (ruleResource != null) {
            return ruleResource.getRuleName();
        }
        return null;
    }

    private void saveTarget(CloudWatchEventsClient client) {

        Target.Builder builder = Target.builder();

        builder = builder.arn(getTargetArn())
                .id(getTargetId())
                .roleArn(getRole() != null ? getRole().getArn() : null)
                .sqsParameters(
                        g -> g.messageGroupId(getMessageGroupId())
                )
                .input(getInput())
                .inputPath(getInputPath()
                );

        if (getEcsTaskCount() != null && getEcsTaskDefinitionArn() != null) {
                builder = builder.ecsParameters(e -> e.taskCount(getEcsTaskCount())
                    .taskDefinitionArn(getEcsTaskDefinitionArn())
                );
        }

        if (!ObjectUtils.isBlank(getKinesisPartitionKeyPath())) {
            builder = builder.kinesisParameters( k -> k.partitionKeyPath(getKinesisPartitionKeyPath()));
        }

        if (!ObjectUtils.isBlank(getInputTransformerTemplate())) {
            builder = builder.inputTransformer(i -> i.inputTemplate(getInputTransformerTemplate())
                            .inputPathsMap(getInputTransformerPathMap())
                    );
        }

        PutTargetsRequest request = PutTargetsRequest.builder()
                .rule(getRuleName())
                .targets(builder.build())
                .build();

        client.putTargets(request);
    }
}

