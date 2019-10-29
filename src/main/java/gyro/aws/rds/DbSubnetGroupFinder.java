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
import software.amazon.awssdk.services.rds.model.DBSubnetGroup;
import software.amazon.awssdk.services.rds.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    db-subnet-group: $(external-query aws::db-subnet-group { name: 'db-subnet-group-db-cluster-example'})
 */
@Type("db-subnet-group")
public class DbSubnetGroupFinder extends AwsFinder<RdsClient, DBSubnetGroup, DbSubnetGroupResource> {

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
    protected List<DBSubnetGroup> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("group-name")) {
            throw new IllegalArgumentException("'group-name' is required.");
        }

        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("group-name"))).dbSubnetGroups();
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(RdsClient client) {
        return client.describeDBSubnetGroupsPaginator().dbSubnetGroups().stream().collect(Collectors.toList());
    }

}
