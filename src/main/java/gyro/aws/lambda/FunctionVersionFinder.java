/*
 * Copyright 2025, Brightspot.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListVersionsByFunctionResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

/**
 * Query lambda function versions.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lambda-function-version: $(external-query aws::lambda-function-version { function-name: '' | version = ''})
 */
@Type("lambda-version")
public class FunctionVersionFinder extends AwsFinder<LambdaClient, FunctionConfiguration, FunctionVersionResource> {

    private String functionName;
    private String version;

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
     * The version number.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    protected List<FunctionConfiguration> findAllAws(LambdaClient client) {
        List<FunctionConfiguration> versions = new ArrayList<>();

        List<String> functionNames = client.listFunctionsPaginator()
            .functions().stream()
            .map(FunctionConfiguration::functionName)
            .collect(Collectors.toList());

        for (String name : functionNames) {
            versions.addAll(getVersionsForFunction(client, name, null));
        }

        return versions;
    }

    @Override
    protected List<FunctionConfiguration> findAws(LambdaClient client, Map<String, String> filters) {
        if (!filters.containsKey("function-name")) {
            throw new IllegalArgumentException("'function-name is required.'");
        }

        return getVersionsForFunction(client, filters.get("function-name"), filters.getOrDefault("version", null));
    }

    private List<FunctionConfiguration> getVersionsForFunction(LambdaClient client, String name, String version) {
        List<FunctionConfiguration> versions = new ArrayList<>();

        try {
            for (ListVersionsByFunctionResponse response : client.listVersionsByFunctionPaginator(
                r -> r.functionName(name))) {
                if (StringUtils.isBlank(version)) {
                    versions.addAll(response.versions());
                } else {
                    versions.addAll(response.versions().stream()
                        .filter(v -> v.version().equals(version))
                        .collect(Collectors.toList()));
                }
            }
        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return versions;
    }
}
