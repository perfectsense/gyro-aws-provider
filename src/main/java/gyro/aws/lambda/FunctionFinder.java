package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
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
 *    lambda-function: $(aws::lambda-function EXTERNAL/* | function-name = '')
 */
@Type("lambda-function")
public class FunctionFinder extends AwsFinder<LambdaClient, FunctionConfiguration, FunctionResource> {
    private String functionName;

    /**
     * The function name.
     */
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    protected List<FunctionConfiguration> findAllAws(LambdaClient client) {
        List<FunctionConfiguration> functionConfigurations = new ArrayList<>();
        ListFunctionsResponse listFunctionsResponse;
        String marker = "";

        do {
            if (ObjectUtils.isBlank(marker)) {
                listFunctionsResponse = client.listFunctions();
            } else {
                listFunctionsResponse = client.listFunctions(ListFunctionsRequest.builder().marker(marker).build());
            }
            marker = listFunctionsResponse.nextMarker();
            functionConfigurations.addAll(listFunctionsResponse.functions());

        } while (!ObjectUtils.isBlank(marker));

        return functionConfigurations;
    }

    @Override
    protected List<FunctionConfiguration> findAws(LambdaClient client, Map<String, String> filters) {
        List<FunctionConfiguration> functionConfigurations = new ArrayList<>();

        if (filters.containsKey("function-name") && !ObjectUtils.isBlank(filters.get("function-name"))) {

            try {
                functionConfigurations.add(client.getFunction(r -> r.functionName(filters.get("function-name"))).configuration());
            } catch (ResourceNotFoundException ignore) {
                // ignore
            }
        }

        return functionConfigurations;
    }
}
