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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbClusterEndpointResponse;
import software.amazon.awssdk.services.rds.model.DBClusterEndpoint;
import software.amazon.awssdk.services.rds.model.DbClusterEndpointNotFoundException;
import software.amazon.awssdk.services.rds.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterEndpointsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a db cluster endpoint.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-cluster-endpoint endpoint-example
 *        identifier: "endpoint"
 *        db-cluster: $(aws::db-cluster db-cluster-example)
 *        endpoint-type: "READER"
 *        static-members: [$(aws::db-instance db-instance-example)]
 *    end
 */
@Type("db-cluster-endpoint")
public class DbClusterEndpointResource extends AwsResource implements Copyable<DBClusterEndpoint> {

    private String identifier;
    private DbClusterResource dbCluster;
    private String endpointType;
    private List<DbInstanceResource> excludedMembers;
    private List<DbInstanceResource> staticMembers;
    private String endpointAddress;

    /**
     * The unique identifier of the endpoint.
     */
    @Required
    @Id
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The DB cluster associated with the endpoint.
     */
    @Required
    public DbClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(DbClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The type of the endpoint. Cannot be set to ``WRITER`` if 'db-cluster' is set to ``Aurora``.
     */
    @Required
    @Updatable
    @ValidStrings({"READER", "WRITER", "ANY"})
    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    /**
     * List of DB instances to be excluded from the custom endpoint group. Only applicable if `static-members` is empty.
     */
    @Updatable
    public List<DbInstanceResource> getExcludedMembers() {
        if (excludedMembers == null) {
            excludedMembers = new ArrayList<>();
        }

        return excludedMembers;
    }

    public void setExcludedMembers(List<DbInstanceResource> excludedMembers) {
        this.excludedMembers = excludedMembers;
    }

    /**
     * List of DB instances that are part of the custom endpoint group.
     */
    @Updatable
    public List<DbInstanceResource> getStaticMembers() {
        if (staticMembers == null) {
            staticMembers = new ArrayList<>();
        }

        return staticMembers;
    }

    public void setStaticMembers(List<DbInstanceResource> staticMembers) {
        this.staticMembers = staticMembers;
    }

    /**
     * DNS address of the endpoint.
     */
    @Output
    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    @Override
    public void copyFrom(DBClusterEndpoint endpoint) {
        setEndpointType(endpoint.customEndpointType());
        setExcludedMembers(endpoint.excludedMembers().stream().map(i -> findById(DbInstanceResource.class, i)).collect(Collectors.toList()));
        setStaticMembers(endpoint.staticMembers().stream().map(i -> findById(DbInstanceResource.class, i)).collect(Collectors.toList()));
        setEndpointAddress(endpoint.endpoint());
    }

    @Override
    public boolean refresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getIdentifier()) || ObjectUtils.isBlank(getDbCluster())) {
            throw new GyroException("name or db-cluster is missing, unable to load db cluster endpoint.");
        }

        try {
            DescribeDbClusterEndpointsResponse response = client.describeDBClusterEndpoints(
                r -> r.dbClusterEndpointIdentifier(getIdentifier())
                        .dbClusterIdentifier(getDbCluster().getIdentifier())
            );

            response.dbClusterEndpoints().forEach(this::copyFrom);

        } catch (DbClusterNotFoundException | DbClusterEndpointNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateDbClusterEndpointResponse response = client.createDBClusterEndpoint(
            r -> r.dbClusterEndpointIdentifier(getIdentifier())
                    .dbClusterIdentifier(getDbCluster().getIdentifier())
                    .endpointType(getEndpointType())
                    .excludedMembers(getExcludedMembers()
                        .stream()
                        .map(DbInstanceResource::getIdentifier)
                        .collect(Collectors.toList()))

                    .staticMembers(getStaticMembers()
                        .stream()
                        .map(DbInstanceResource::getIdentifier)
                        .collect(Collectors.toList()))
        );

        setEndpointAddress(response.endpoint());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBClusterEndpoint(
            r -> r.dbClusterEndpointIdentifier(getIdentifier())
                    .endpointType(getEndpointType())
                    .excludedMembers(getExcludedMembers()
                        .stream()
                        .map(DbInstanceResource::getIdentifier)
                        .collect(Collectors.toList()))

                    .staticMembers(getStaticMembers()
                        .stream()
                        .map(DbInstanceResource::getIdentifier)
                        .collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBClusterEndpoint(
            r -> r.dbClusterEndpointIdentifier(getIdentifier())
        );
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        // Add validation for Aurora DB and endpointType 'WRITER'
        String engine = getDbCluster().getEngine();
        if ((engine.equalsIgnoreCase("aurora-mysql") || engine.equalsIgnoreCase("aurora-postgresql"))
                && getEndpointType().equalsIgnoreCase("WRITER")) {
            errors.add(new ValidationError(
                    this,
                    "dbCluster",
                    "Database engine " + engine + " with endpointType 'WRITER' is not supported"));
        }

        return errors;
    }
}
