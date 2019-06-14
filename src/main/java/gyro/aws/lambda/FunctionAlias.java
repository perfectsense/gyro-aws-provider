package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateAliasRequest;
import software.amazon.awssdk.services.lambda.model.CreateAliasResponse;
import software.amazon.awssdk.services.lambda.model.GetAliasResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateAliasRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Creates an function alias.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-alias lambda-alias-example
 *         alias-name: "lambda-alias-example"
 *         function: $(aws::lambda-function lambda-function-event-source-mapping-example)
 *         function-version: "10"
 *         description: "lambda-alias-example"
 *         additional-version: "9"
 *         weight: 0.4
 *     end
 */
@Type("lambda-alias")
public class FunctionAlias extends AwsResource implements Copyable<GetAliasResponse> {
    private String aliasName;
    private FunctionResource function;
    private String functionVersion;
    private String description;
    private String additionalVersion;
    private Double weight;

    private String arn;
    private String revisionId;

    /**
     * Name of the Lambda Alias. (Required)
     */
    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * The Lambda Function for the Lambda Alias. (Required)
     */
    public FunctionResource getFunction() {
        return function;
    }

    public void setFunction(FunctionResource function) {
        this.function = function;
    }

    /**
     * The Lambda Function version for the Lambda Alias. (Required)
     */
    @Updatable
    public String getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(String functionVersion) {
        this.functionVersion = functionVersion;
    }

    /**
     * The description for the Lambda Alias.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Secondary Lambda Function versions for the Lambda Alias.
     */
    @Updatable
    public String getAdditionalVersion() {
        return additionalVersion;
    }

    public void setAdditionalVersion(String additionalVersion) {
        this.additionalVersion = additionalVersion;
    }

    /**
     * The weight to switch between the secondary version. Required if additional version set. Valid values are between ``0.0`` to ``1.0``
     */
    @Updatable
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * The arn of the Lambda Alias.
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
     * The revision ID of the Lambda Alias.
     */
    @Output
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @Override
    public void copyFrom(GetAliasResponse response) {
        setAliasName(response.name());
        setArn(response.aliasArn());
        setDescription(response.description());
        setFunctionVersion(response.functionVersion());
        setRevisionId(response.revisionId());
        if (response.routingConfig() != null && response.routingConfig().additionalVersionWeights().isEmpty()) {
            setAdditionalVersion(response.routingConfig().additionalVersionWeights().keySet().iterator().next());
            setWeight(response.routingConfig().additionalVersionWeights().get(getAdditionalVersion()));
        }
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetAliasResponse response = client.getAlias(
                r -> r.name(getAliasName())
                    .functionName(getFunction().getFunctionName())
            );

            copyFrom(response);
        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create() {
        LambdaClient client = createClient(LambdaClient.class);

        CreateAliasRequest.Builder builder = CreateAliasRequest.builder()
            .name(getAliasName())
            .description(getDescription())
            .functionName(getFunction().getFunctionName())
            .functionVersion(getFunctionVersion());

        if (!ObjectUtils.isBlank(getAdditionalVersion())) {
            Map<String, Double> map = new HashMap<>();
            map.put(getAdditionalVersion(), getWeight());
            builder = builder.routingConfig(
                r -> r.additionalVersionWeights(map)
            );
        }

        CreateAliasResponse response = client.createAlias(builder.build());

        setArn(response.aliasArn());
        setRevisionId(response.revisionId());
    }

    @Override
    public void update(Resource resource, Set<String> changedFieldNames) {
        LambdaClient client = createClient(LambdaClient.class);

        UpdateAliasRequest.Builder builder = UpdateAliasRequest.builder()
            .revisionId(getRevisionId())
            .name(getAliasName())
            .description(getDescription())
            .functionName(getFunction().getFunctionName())
            .functionVersion(getFunctionVersion());

        if (!ObjectUtils.isBlank(getAdditionalVersion())) {
            Map<String, Double> map = new HashMap<>();
            map.put(getAdditionalVersion(), getWeight());
            builder = builder.routingConfig(
                r -> r.additionalVersionWeights(map)
            );
        }

        client.updateAlias(builder.build());
    }

    @Override
    public void delete() {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteAlias(
            r -> r.name(getAliasName())
                .functionName(getFunction().getFunctionName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("lambda function alias");

        if (!ObjectUtils.isBlank(getAliasName())) {
            sb.append(" - ").append(getAliasName());
        }

        if (getFunction() != null && !ObjectUtils.isBlank(getFunction().getFunctionName())) {
            sb.append(", function - ").append(getFunction().getFunctionName());
        }

        return sb.toString();
    }
}
