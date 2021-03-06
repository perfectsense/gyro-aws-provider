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
import software.amazon.awssdk.services.rds.model.DBClusterEndpoint;

import java.util.List;
import java.util.Map;

/**
 * Query db cluster endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    endpoints: $(external-query aws::db-cluster-endpoint { db-cluster-endpoint-type: 'reader'})
 */
@Type("db-cluster-endpoint")
public class DbClusterEndpointFinder extends AwsFinder<RdsClient, DBClusterEndpoint, DbClusterEndpointResource> {

    private String dbClusterEndpointType;
    private String dbClusterEndpointCustomType;
    private String dbClusterEndpointId;
    private String dbClusterEndpointStatus;

    /**
     * The type of the endpoint. Valid values are ``reader``, ``writer`` and ``custom``.
     */
    public String getDbClusterEndpointType() {
        return dbClusterEndpointType;
    }

    public void setDbClusterEndpointType(String dbClusterEndpointType) {
        this.dbClusterEndpointType = dbClusterEndpointType;
    }

    /**
     * The custom type of the endpoint. Valid values are ``reader`` and ``any``.
     */
    public String getDbClusterEndpointCustomType() {
        return dbClusterEndpointCustomType;
    }

    public void setDbClusterEndpointCustomType(String dbClusterEndpointCustomType) {
        this.dbClusterEndpointCustomType = dbClusterEndpointCustomType;
    }

    /**
     * The identifier of the endpoint.
     */
    public String getDbClusterEndpointId() {
        return dbClusterEndpointId;
    }

    public void setDbClusterEndpointId(String dbClusterEndpointId) {
        this.dbClusterEndpointId = dbClusterEndpointId;
    }

    /**
     * The status of the endpoint. Valid values are ``available``, ``creating``, ``deleting`` and ``modifying``.
     */
    public String getDbClusterEndpointStatus() {
        return dbClusterEndpointStatus;
    }

    public void setDbClusterEndpointStatus(String dbClusterEndpointStatus) {
        this.dbClusterEndpointStatus = dbClusterEndpointStatus;
    }

    @Override
    protected List<DBClusterEndpoint> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBClusterEndpoints(r -> r.filters(createRdsFilters(filters))).dbClusterEndpoints();
    }

    @Override
    protected List<DBClusterEndpoint> findAllAws(RdsClient client) {
        return client.describeDBClusterEndpoints().dbClusterEndpoints();
    }

}
