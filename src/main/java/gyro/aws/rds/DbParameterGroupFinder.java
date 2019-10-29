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

package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBParameterGroup;
import software.amazon.awssdk.services.rds.model.DbParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    db-parameter-groups: $(external-query aws::db-parameter-group { name: 'db-parameter-group-example'})
 */
@Type("db-parameter-group")
public class DbParameterGroupFinder extends AwsFinder<RdsClient, DBParameterGroup, DbParameterGroupResource> {

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
    protected List<DBParameterGroup> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBParameterGroups(r -> r.dbParameterGroupName(filters.get("name"))).dbParameterGroups();
        } catch (DbParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBParameterGroup> findAllAws(RdsClient client) {
        return client.describeDBParameterGroupsPaginator().dbParameterGroups().stream().collect(Collectors.toList());
    }

}
