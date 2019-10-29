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

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.docdb.model.DbParameterGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParameterGroupsRequest;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParameterGroupsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("docdb-cluster-param-group")
public class DbClusterParameterGroupFinder extends DocDbFinder<DocDbClient, DBClusterParameterGroup, DbClusterParameterGroupResource> {

    private String name;

    /**
     * The name of a Document DB cluster parameter group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterParameterGroup> findAllAws(DocDbClient client) {
        List<DBClusterParameterGroup> dbClusterParameterGroups = new ArrayList<>();
        String marker = null;
        DescribeDbClusterParameterGroupsResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeDBClusterParameterGroups();
            } else {
                response = client.describeDBClusterParameterGroups(DescribeDbClusterParameterGroupsRequest.builder().marker(marker).build());
            }

            marker = response.marker();
            dbClusterParameterGroups.addAll(response.dbClusterParameterGroups());
        } while (!ObjectUtils.isBlank(marker));

        return dbClusterParameterGroups;
    }

    @Override
    protected List<DBClusterParameterGroup> findAws(DocDbClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBClusterParameterGroups(r -> r.dbClusterParameterGroupName(filters.get("name"))).dbClusterParameterGroups();
        } catch (DbParameterGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
