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

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dax.DaxClient;
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
     * The list of parameter group names.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ParameterGroup> findAllAws(DaxClient client) {
        return client.describeParameterGroups().parameterGroups();
    }

    @Override
    protected List<ParameterGroup> findAws(
        DaxClient client, Map<String, String> filters) {
        return client.describeParameterGroups(r -> r.parameterGroupNames(filters.get("name"))).parameterGroups();
    }
}
