/*
 * Copyright 2025, Brightspot, Inc.
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

package gyro.aws.ssm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.GyroException;
import gyro.core.Type;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DescribeParametersRequest;
import software.amazon.awssdk.services.ssm.model.DescribeParametersResponse;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;

/**
 * Query for SSM parameters.
 * <p>
 * The filters are specified as a list of maps, where each map contains the following:<br>
 * <ul>
 *     <li><strong>key</strong> - The name of the filter key ("Name", "Type", or "KeyId").</li>
 *     <li><strong>option</strong> - (Optional) The filter option ("Equals", "BeginsWith", "Contains", "Recursive", or "OneLevel" )</li>
 *     <li><strong>values</strong> - The list of values to filter by.</li>
 * </ul>
 * </p>
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    parameter: $(external-query aws::ssm-parameter {
 *        parameter-filters: [
 *            {
 *                key: "Name",
 *                option: "Equals",
 *                values: ["my-parameter"]
 *            },
 *            {
 *                key: "Type",
 *                values: ["String"]
 *            }
 *        ]
 *    })
 */
@Type("ssm-parameter")
public class SsmParameterFinder extends AwsFinder<SsmClient, ParameterMetadata, SsmParameterResource> {

    @Override
    protected List<ParameterMetadata> findAllAws(SsmClient client) {
        List<ParameterMetadata> parameters = new ArrayList<>();
        String nextToken = null;
        do {
            DescribeParametersRequest.Builder request = DescribeParametersRequest.builder();
            if (nextToken != null) {
                request.nextToken(nextToken);
            }
            DescribeParametersResponse response = client.describeParameters(request.build());
            parameters.addAll(response.parameters());
            nextToken = response.nextToken();
        } while (nextToken != null);

        return parameters;
    }

    @Override
    protected List<ParameterMetadata> findAws(SsmClient client, Map<String, String> filters) {
        throw new GyroException(
            "'parameter-filters' cannot be for type Map<String, String> for SSM parameters. Use List<Map<String, <String | List<Sting>>>> instead.");
    }

    protected List<ParameterMetadata> findAws(SsmClient client, List<ParameterStringFilter> filters) {
        DescribeParametersRequest.Builder request = DescribeParametersRequest.builder()
            .parameterFilters(filters);

        DescribeParametersResponse response = client.describeParameters(request.build());
        return response.parameters();
    }

    @Override
    public List<SsmParameterResource> find(Map<String, Object> filters) {
        return findAws(newClient(), convertFilters(filters)).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    /**
     * Convert {tagKey: tagValue} to {tag:Key: tagValue}
     */
    @SuppressWarnings("unchecked")
    private List<ParameterStringFilter> convertFilters(Map<String, Object> query) {
        ArrayList<ParameterStringFilter> parameterFitlers = new ArrayList<>();

        try {
            for (Map.Entry<String, Object> e : query.entrySet()) {
                if ("parameter-filters".equalsIgnoreCase(e.getKey()) && e.getValue() != null) {
                    List<Object> filterList = (List<Object>) e.getValue();
                    if (!filterList.isEmpty()) {
                        for (Object filter : filterList) {
                            if (filter instanceof Map) {
                                Map<String, Object> filterMap = (Map<String, Object>) filter;
                                String key = (String) filterMap.get("key");
                                String option = (String) filterMap.getOrDefault("option", null);
                                List<String> values = (List<String>) filterMap.get("values");

                                ParameterStringFilter.Builder parameterFilterBuilder =
                                    ParameterStringFilter.builder().key(key).option(option).values(values);

                                if (option != null) {
                                    parameterFilterBuilder.option(option);
                                }

                                parameterFitlers.add(parameterFilterBuilder.build());
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new GyroException(
                "Invalid filter format for 'parameter-filters'. Expected a list of format List<Map<String, <String | List<Sting>>>>.",
                e);
        }

        return parameterFitlers;
    }
}
