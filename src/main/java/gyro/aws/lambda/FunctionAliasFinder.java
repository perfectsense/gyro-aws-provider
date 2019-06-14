package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.AliasConfiguration;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.GetAliasResponse;
import software.amazon.awssdk.services.lambda.model.ListAliasesRequest;
import software.amazon.awssdk.services.lambda.model.ListAliasesResponse;
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.sns.model.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query lambda alias.
 *
 * .. code-block:: gyro
 *
 *    lambda-alias: $(aws::lambda-alias EXTERNAL/* | function-name = '' | alias-name = '')
 */
@Type("lambda-alias")
public class FunctionAliasFinder extends AwsFinder<LambdaClient, GetAliasResponse, FunctionAlias> {
    private String functionName;
    private String aliasName;

    /**
     * The function name.
     */
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The alias name.
     */
    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    protected List<GetAliasResponse> findAllAws(LambdaClient client) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        List<String> functionNames = new ArrayList<>();
        ListFunctionsResponse listFunctionsResponse;
        String marker = "";

        do {
            if (ObjectUtils.isBlank(marker)) {
                listFunctionsResponse = client.listFunctions();
            } else {
                listFunctionsResponse = client.listFunctions(ListFunctionsRequest.builder().marker(marker).build());
            }
            marker = listFunctionsResponse.nextMarker();
            functionNames.addAll(
                listFunctionsResponse.functions().stream()
                    .map(FunctionConfiguration::functionName)
                    .collect(Collectors.toList())
            );
        } while (!ObjectUtils.isBlank(marker));

        for (String functionName : functionNames) {
            getAliasResponses.addAll(getAllAliasForFunction(client, functionName));
        }

        return getAliasResponses;
    }

    @Override
    protected List<GetAliasResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        if (filters.containsKey("function-name") && !ObjectUtils.isBlank(filters.get("function-name"))) {
            if (filters.containsKey("alias-name") && !ObjectUtils.isBlank(filters.get("alias-name"))) {
                try {
                    getAliasResponses.add(
                        client.getAlias(
                            r -> r.functionName(filters.get("function-name"))
                                .name(filters.get("alias-name"))
                        )
                    );
                } catch (NotFoundException ignore) {
                    // ignore
                }
            } else {
                getAliasResponses = getAllAliasForFunction(client, filters.get("function-name"));
            }
        }

        return getAliasResponses;
    }

    private GetAliasResponse toGetAliasResponse(AliasConfiguration aliasConfiguration) {
        return GetAliasResponse.builder()
            .aliasArn(aliasConfiguration.aliasArn())
            .description(aliasConfiguration.description())
            .functionVersion(aliasConfiguration.functionVersion())
            .name(aliasConfiguration.name())
            .revisionId(aliasConfiguration.revisionId())
            .routingConfig(aliasConfiguration.routingConfig())
            .build();
    }

    private List<GetAliasResponse> getAllAliasForFunction(LambdaClient client, String functionName) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        ListAliasesResponse response;
        String marker = "";

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listAliases(ListAliasesRequest.builder().functionName(functionName).build());
            } else {
                response = client.listAliases(ListAliasesRequest.builder().functionName(functionName).marker(marker).build());
            }

            marker = response.nextMarker();
            getAliasResponses.addAll(response.aliases().stream().map(this::toGetAliasResponse).collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        return getAliasResponses;
    }
}
