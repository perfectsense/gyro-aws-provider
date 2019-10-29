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

package gyro.aws.docdb;

import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DbSubnetGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("docdb-subnet-group")
public class DbSubnetGroupFinder extends DocDbFinder<DocDbClient, DBSubnetGroup, DbSubnetGroupResource> {

    private String name;

    /**
     * The DocumentDB subnet group name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBSubnetGroup> findAllAws(DocDbClient client) {
        return client.describeDBSubnetGroupsPaginator().dbSubnetGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBSubnetGroup> findAws(DocDbClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBSubnetGroups(r -> r.dbSubnetGroupName(filters.get("name"))).dbSubnetGroups();
        } catch (DbSubnetGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

}
