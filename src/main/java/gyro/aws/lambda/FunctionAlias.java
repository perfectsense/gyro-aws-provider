package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
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
 *         function-name: "testFunction"
 *         function-version: "10"
 *         description: "lambda-alias-example"
 *         additional-version: "9"
 *         weight: 0.4
 *     end
 */
@ResourceType("lambda-alias")
public class FunctionAlias extends AwsResource {
    private String aliasName;
    private String functionName;
    private String functionVersion;
    private String description;
    private String additionalVersion;
    private Double weight;

    private String arn;
    private String revisionId;

    /**
     * Name of the alias. (Required)
     */
    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * Name of the function for the alias. (Required)
     */
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The function version for the alias. (Required)
     */
    @ResourceUpdatable
    public String getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(String functionVersion) {
        this.functionVersion = functionVersion;
    }

    /**
     * The description for the alias.
     */
    @ResourceUpdatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Secondary function for the alias.
     */
    @ResourceUpdatable
    public String getAdditionalVersion() {
        return additionalVersion;
    }

    public void setAdditionalVersion(String additionalVersion) {
        this.additionalVersion = additionalVersion;
    }

    /**
     * The weight to switch between the secondary version. Required if additional version set. Valid values between ``0.0`` to ``1.0``
     */
    @ResourceUpdatable
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * The arn of the alias.
     */
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The revision id of the alias.
     */
    @ResourceOutput
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetAliasResponse response = client.getAlias(
                r -> r.name(getAliasName())
                    .functionName(getFunctionName())
            );

            setArn(response.aliasArn());
            setDescription(response.description());
            setFunctionVersion(response.functionVersion());
            setRevisionId(response.revisionId());
            if (response.routingConfig() != null && response.routingConfig().additionalVersionWeights().isEmpty()) {
                setAdditionalVersion(response.routingConfig().additionalVersionWeights().keySet().iterator().next());
                setWeight(response.routingConfig().additionalVersionWeights().get(getAdditionalVersion()));
            }
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
            .functionName(getFunctionName())
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
    public void update(Resource resource, Set<String> set) {
        LambdaClient client = createClient(LambdaClient.class);

        UpdateAliasRequest.Builder builder = UpdateAliasRequest.builder()
            .revisionId(getRevisionId())
            .name(getAliasName())
            .description(getDescription())
            .functionName(getFunctionName())
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
                .functionName(getFunctionName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("lambda function alias");

        if (!ObjectUtils.isBlank(getAliasName())) {
            sb.append(" - ").append(getAliasName());
        }

        if (!ObjectUtils.isBlank(getFunctionName())) {
            sb.append(", function - ").append(getFunctionName());
        }

        return sb.toString();
    }
}
