package gyro.aws.lambda;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query lambda function.
 *
 * .. code-block:: gyro
 *
 *    lambda-function: $(aws::lambda-function EXTERNAL/* | name = '')
 */
@Type("lambda-function")
public class FunctionFinder extends AwsFinder<LambdaClient, FunctionConfiguration, FunctionResource> {
    private String name;

    /**
     * The function name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<FunctionConfiguration> findAllAws(LambdaClient client) {
        return client.listFunctionsPaginator().functions().stream().collect(Collectors.toList());
    }

    @Override
    protected List<FunctionConfiguration> findAws(LambdaClient client, Map<String, String> filters) {
        List<FunctionConfiguration> functionConfigurations = new ArrayList<>();

        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("name is required.");
        }

        try {
            functionConfigurations.add(client.getFunction(r -> r.functionName(filters.get("name"))).configuration());
        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return functionConfigurations;
    }
}
