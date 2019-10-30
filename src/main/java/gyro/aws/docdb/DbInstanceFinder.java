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
import software.amazon.awssdk.services.docdb.model.DBInstance;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("docdb-instance")
public class DbInstanceFinder extends DocDbFinder<DocDbClient, DBInstance, DbInstanceResource> {

    private String dbClusterId;
    private String dbInstanceId;

    /**
     * The DocumentDB cluster identifier or arn associated to an instance.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The DocumentDB instance identifier or arn.
     */
    public String getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(String dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    @Override
    protected List<DBInstance> findAllAws(DocDbClient client) {
        return client.describeDBInstancesPaginator().dbInstances().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBInstance> findAws(DocDbClient client, Map<String, String> filters) {
        return client.describeDBInstancesPaginator(r -> r.filters(createDocDbFilters(filters))).dbInstances().stream().collect(Collectors.toList());
    }

}
