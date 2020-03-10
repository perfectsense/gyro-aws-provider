/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.neptune.model.DbClusterParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query Neptune cluster parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    neptune-cluster-parameter-group: $(external-query aws::neptune-cluster-parameter-group {name: "neptune-cluster-parameter-group-example"})
 */
@Type("neptune-cluster-parameter-group")
public class NeptuneClusterParameterGroupFinder extends AwsFinder<NeptuneClient, DBClusterParameterGroup, NeptuneClusterParameterGroupResource> {

    private String name;

    /**
     * The name of a Neptune cluster parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(NeptuneClient client) {
        return client.describeDBClusterParameterGroups()
            .dbClusterParameterGroups().stream()
            .filter(p -> p.dbParameterGroupFamily().contains("neptune"))
            .collect(Collectors.toList());
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(NeptuneClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name")))
                .dbClusterParameterGroups().stream()
                .filter(p -> p.dbParameterGroupFamily().contains("neptune"))
                .collect(Collectors.toList());

        } catch (DbClusterParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
