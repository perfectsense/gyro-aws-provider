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
import software.amazon.awssdk.services.neptune.model.DBSubnetGroup;
import software.amazon.awssdk.services.neptune.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query Neptune subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    neptune-subnet-group: $(external-query aws::neptune-subnet-group {name: 'neptune-subnet-group-example'})
 */
@Type("neptune-subnet-group")
public class NeptuneSubnetGroupFinder extends AwsFinder<NeptuneClient, DBSubnetGroup, NeptuneSubnetGroupResource> {

    private String name;

    /**
     * The name of the subnet group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(NeptuneClient client) {
        return client.describeDBSubnetGroups().dbSubnetGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBSubnetGroup> findAws(NeptuneClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("name"))).dbSubnetGroups().stream().collect(Collectors.toList());
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
