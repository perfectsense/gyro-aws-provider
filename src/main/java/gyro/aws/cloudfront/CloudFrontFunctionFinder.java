/*
 * Copyright 2023, Brightspot.
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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.FunctionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListFunctionsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListFunctionsResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchFunctionExistsException;

/**
 * Query cloudfront function.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cloudfront-function: $(external-query aws::cloudfront-function { })
 */
@Type("cloudfront-function")
public class CloudFrontFunctionFinder extends AwsFinder<CloudFrontClient, FunctionSummary, CloudFrontFunctionResource> {

    private String name;

    /**
     * The name of the function.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<FunctionSummary> findAllAws(CloudFrontClient client) {
        List<FunctionSummary> functions = new ArrayList<>();

        String marker = null;
        ListFunctionsResponse response = client.listFunctions(ListFunctionsRequest.builder().marker(marker).build());

        if (response.functionList() != null && response.functionList().items() != null) {
            functions.addAll(response.functionList().items());
        }

        return functions;
    }

    @Override
    protected List<FunctionSummary> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<FunctionSummary> functions = new ArrayList<>();

        if (filters.containsKey("name")) {
            String name = filters.get("name");
            try {
                functions.add(client.describeFunction(r -> r.name(name).build()).functionSummary());
            } catch (NoSuchFunctionExistsException ignore) {
                // ignore
            }
        }

        return functions;
    }

    @Override
    protected String getRegion() {
        return "us-east-1";
    }

    @Override
    protected String getEndpoint() {
        return "https://cloudfront.amazonaws.com";
    }
}
