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
import software.amazon.awssdk.services.rds.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.rds.model.DbParameterGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query db cluster parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cluster-parameter-groups: $(external-query aws::db-cluster-parameter-group { name: 'cluster-parameter-group-example'})
 */
@Type("db-cluster-parameter-group")
public class DbClusterParameterGroupFinder extends AwsFinder<RdsClient, DBClusterParameterGroup, DbClusterParameterGroupResource> {

    private String name;

    /**
     * The name of the cluster parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name"))).dbClusterParameterGroups();
        } catch (DbParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(RdsClient client) {
        return client.describeDBClusterParameterGroups().dbClusterParameterGroups();
    }

}
