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
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lambda-function: $(external-query aws::lambda-function { name: ''})
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
