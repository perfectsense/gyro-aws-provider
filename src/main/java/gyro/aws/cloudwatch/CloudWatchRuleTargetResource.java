package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CloudWatchRuleTargetResource extends AwsResource {

    private String targetId;
    private String targetArn;
    private String roleArn;
    private String messageGroupId;
    private Integer ecsTaskCount;
    private String ecsTaskDefinitionArn;
    private String input;
    private String inputPath;
    private Map<String, String> inputTransformerPathMap;
    private String inputTransformerTemplate;
    private String kinesisPartitionKeyPath;

    public CloudWatchRuleTargetResource() {}

    public CloudWatchRuleTargetResource(Target target) {
        setTargetId(target.id());
        setTargetArn(target.arn());
        setRoleArn(target.roleArn());
        setMessageGroupId(target.sqsParameters().messageGroupId());
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

    /**
     * The identifier of the target resource. (Required)
     *
     * It can include alphanumeric characters, periods (.), hyphens (-), and underscores (_).
     */
    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * The arn of the target resource, also served as its identifier and thus unique. (Required)
     */
    @ResourceUpdatable
    public String getTargetArn() {
        return targetArn;
    }

    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    /**
     * The IAM role arn that gives permission to invoke actions on the target resource. (Optional)
     */
    @ResourceUpdatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * The input json text that triggers an action on the target resource. If provided, doesn't consider the matched event conditions. (Optional)
     */
    @ResourceUpdatable
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    /**
     * The input path that passes the part of the event to trigger the target resource. (Optional)
     */
    @ResourceUpdatable
    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    /**
     * Thes SQS queue message group id which will be the destination for the target when triggered.
     *
     * The SQS queue must be a FIFO type and should have content-based-deduplication enabled.
     */
    @ResourceUpdatable
    public String getMessageGroupId() {
        return messageGroupId;
    }

    public void setMessageGroupId(String messageGroupId) {
        this.messageGroupId = messageGroupId;
    }

    /**
     * The number of tasks created when the target is an Amazon ECS task. (Default is `1`)
     */
    @ResourceUpdatable
    public Integer getEcsTaskCount() {
        return ecsTaskCount;
    }

    public void setEcsTaskCount(Integer ecsTaskCount) {
        this.ecsTaskCount = ecsTaskCount;
    }

    /**
     * The arn of the task that is created when the target is an Amazon ECS task.
     */
    @ResourceUpdatable
    public String getEcsTaskDefinitionArn() {
        return ecsTaskDefinitionArn;
    }

    public void setEcsTaskDefinitionArn(String ecsTaskDefinitionArn) {
        this.ecsTaskDefinitionArn = ecsTaskDefinitionArn;
    }

    /**
     * The map of customized inputs that can be used to configure cloudwatch event targets.
     *
     * See `AWS Services CloudWatch InputTransformer property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-inputtransformer.html/>`_.
     */
    @ResourceUpdatable
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
     * The input template which contains the values of the keys of the input path maps.
     *
     * See `AWS Services CloudWatch InputTransformer property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-inputtransformer.html/>`_.
     */
    @ResourceUpdatable
    public String getInputTransformerTemplate() {
        return inputTransformerTemplate;
    }

    public void setInputTransformerTemplate(String inputTransformerTemplate) {
        this.inputTransformerTemplate = inputTransformerTemplate;
    }

    /**
     * The path of the kinesis data stream target.
     *
     * See `AWS Services CloudWatch Kinesis parameter property <https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-events-rule-kinesisparameters.html/>`_.
     *
     */
    @ResourceUpdatable
    public String getKinesisPartitionKeyPath() {
        return kinesisPartitionKeyPath;
    }

    public void setKinesisPartitionKeyPath(String kinesisPartitionKeyPath) {
        this.kinesisPartitionKeyPath = kinesisPartitionKeyPath;
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
        return String.format("%s", getTargetId());
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

        Target.Builder builder = Target.builder();

        builder = builder.arn(getTargetArn())
                .id(getTargetId())
                .roleArn(getRoleArn())
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

        if (!(ObjectUtils.isBlank(getInputTransformerPathMap()) && ObjectUtils.isBlank(getInputTransformerTemplate()))) {
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

