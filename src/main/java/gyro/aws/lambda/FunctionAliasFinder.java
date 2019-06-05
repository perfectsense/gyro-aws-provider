package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetAliasResponse;
import software.amazon.awssdk.services.sns.model.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return Collections.emptyList();
    }

    @Override
    protected List<GetAliasResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        if (filters.containsKey("function-name") && !ObjectUtils.isBlank(filters.get("function-name"))
            && filters.containsKey("alias-name") && !ObjectUtils.isBlank(filters.get("alias-name"))) {
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
        }

        return getAliasResponses;
    }
}
