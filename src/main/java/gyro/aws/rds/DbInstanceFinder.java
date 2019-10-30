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
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DbInstanceNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    db-instances: $(external-query aws::db-instance { db-instance-id: 'db-instance-example'})
 */
@Type("db-instance")
public class DbInstanceFinder extends AwsFinder<RdsClient, DBInstance, DbInstanceResource> {

    private String dbClusterId;
    private String dbInstanceId;

    /**
     * The identifier or arn of the cluster.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The identifier or arn of the db instance.
     */
    public String getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(String dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    @Override
    protected List<DBInstance> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeDBInstances(r -> r.filters(createRdsFilters(filters))).dbInstances();
        } catch (DbInstanceNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBInstance> findAllAws(RdsClient client) {
        return client.describeDBInstancesPaginator().dbInstances().stream().collect(Collectors.toList());
    }

}
