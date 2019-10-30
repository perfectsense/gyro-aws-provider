/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import software.amazon.awssdk.services.sns.model.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query lambda alias.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lambda-alias: $(external-query aws::lambda-alias { function-name: '' | name = ''})
 */
@Type("lambda-alias")
public class FunctionAliasFinder extends AwsFinder<LambdaClient, GetAliasResponse, FunctionAlias> {
    private String functionName;
    private String name;

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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<GetAliasResponse> findAllAws(LambdaClient client) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        List<String> functionNames = client.listFunctionsPaginator()
            .functions().stream()
            .map(FunctionConfiguration::functionName)
            .collect(Collectors.toList());

        for (String functionName : functionNames) {
            getAliasResponses.addAll(getAllAliasForFunction(client, functionName));
        }

        return getAliasResponses;
    }

    @Override
    protected List<GetAliasResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetAliasResponse> getAliasResponses = new ArrayList<>();

        if (!filters.containsKey("function-name")) {
            throw new IllegalArgumentException("'function-name is required.'");
        }

        if (filters.containsKey("name")) {
            try {
                getAliasResponses.add(
                    client.getAlias(
                        r -> r.functionName(filters.get("function-name"))
                            .name(filters.get("name"))
                    )
                );
            } catch (NotFoundException ignore) {
                // ignore
            }
        } else {
            getAliasResponses = getAllAliasForFunction(client, filters.get("function-name"));
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
