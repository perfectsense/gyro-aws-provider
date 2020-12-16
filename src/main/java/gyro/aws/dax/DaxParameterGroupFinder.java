/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeParameterGroupsRequest;
import software.amazon.awssdk.services.dax.model.DescribeParameterGroupsResponse;
import software.amazon.awssdk.services.dax.model.ParameterGroup;

/**
 * Query DAX parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dax-parameter-group: $(external-query aws::dax-parameter-group { name: "parameter-group-example"})
 */
@Type("dax-parameter-group")
public class DaxParameterGroupFinder extends AwsFinder<DaxClient, ParameterGroup, DaxParameterGroupResource> {

    private String name;

    /**
     * The name of the parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ParameterGroup> findAllAws(DaxClient client) {
        List<ParameterGroup> parameterGroups = new ArrayList<>();
        DescribeParameterGroupsResponse response;
        String token = null;

        do {
            if (ObjectUtils.isBlank(token)) {
                response = client.describeParameterGroups();
            } else {
                response = client.describeParameterGroups(DescribeParameterGroupsRequest.builder()
                    .nextToken(token)
                    .build());
            }

            if (response.hasParameterGroups()) {
                parameterGroups.addAll(response.parameterGroups());
            }

            token = response.nextToken();
        } while (!ObjectUtils.isBlank(token));

        return parameterGroups;
    }

    @Override
    protected List<ParameterGroup> findAws(
        DaxClient client, Map<String, String> filters) {
        return client.describeParameterGroups(r -> r.parameterGroupNames(filters.get("name"))).parameterGroups();
    }
}
