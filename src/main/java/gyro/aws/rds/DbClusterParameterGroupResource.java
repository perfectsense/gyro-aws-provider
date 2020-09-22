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

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbClusterParameterGroupResponse;
import software.amazon.awssdk.services.rds.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.rds.model.DbParameterGroupNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterParameterGroupsResponse;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterParametersResponse;
import software.amazon.awssdk.services.rds.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a cluster parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-cluster-parameter-group cluster-parameter-group
 *        name: "cluster-parameter-group-example"
 *        description: "some description"
 *        family: "aurora5.6"
 *        parameter
 *            name: "autocommit"
 *            value: "1"
 *        end
 *
 *        parameter
 *            name: "character_set_client"
 *            value: "utf8"
 *        end
 *
 *        tags: {
 *            Name: "cluster-parameter-group-example"
 *        }
 *    end
 */
@Type("db-cluster-parameter-group")
public class DbClusterParameterGroupResource extends RdsTaggableResource implements Copyable<DBClusterParameterGroup> {

    private String description;
    private String family;
    private String name;
    private List<DbParameter> parameter;

    /**
     * The description of the cluster parameter group. (Required)
     */
    @Required
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the cluster parameter group family. (Required)
     */
    @Required
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * The name of the cluster parameter group. (Required)
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of cluster parameters.
     *
     * @subresource gyro.aws.rds.DbParameter
     */
    @Updatable
    public List<DbParameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<>();
        }

        return parameter;
    }

    public void setParameter(List<DbParameter> parameter) {
        this.parameter = parameter;
    }

    @Override
    public void copyFrom(DBClusterParameterGroup group) {
        setFamily(group.dbParameterGroupFamily());
        setName(group.dbClusterParameterGroupName());
        setDescription(group.description());
        setArn(group.dbClusterParameterGroupArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load cluster parameter group.");
        }

        try {
            DescribeDbClusterParameterGroupsResponse response = client.describeDBClusterParameterGroups(
                r -> r.dbClusterParameterGroupName(getName())
            );

            response.dbClusterParameterGroups().forEach(this::copyFrom);

            DescribeDbClusterParametersResponse parametersResponse = client.describeDBClusterParameters(
                r -> r.dbClusterParameterGroupName(getName())
            );

            Set<String> names = getParameter().stream().map(DbParameter::getName).collect(Collectors.toSet());
            getParameter().clear();
            getParameter().addAll(parametersResponse.parameters().stream()
                .filter(p -> names.contains(p.parameterName()))
                .map(p -> {
                    DbParameter parameter = new DbParameter();
                    parameter.setApplyMethod(p.applyMethodAsString());
                    parameter.setName(p.parameterName());
                    parameter.setValue(p.parameterValue());
                    return parameter;
                })
                .collect(Collectors.toList())
            );

        } catch (DbParameterGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateDbClusterParameterGroupResponse response = client.createDBClusterParameterGroup(
            r -> r.dbParameterGroupFamily(getFamily())
                .dbClusterParameterGroupName(getName())
                .description(getDescription())
        );

        setArn(response.dbClusterParameterGroup().dbClusterParameterGroupArn());
        modifyClusterParameterGroup();
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        modifyClusterParameterGroup();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBClusterParameterGroup(r -> r.dbClusterParameterGroupName(getName()));
    }

    private void modifyClusterParameterGroup() {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBClusterParameterGroup(
            r -> r.dbClusterParameterGroupName(getName())
                .parameters(getParameter().stream().map(
                    p -> Parameter.builder()
                        .parameterName(p.getName())
                        .parameterValue(p.getValue())
                        .applyMethod(p.getApplyMethod())
                        .build())
                    .collect(Collectors.toList()))
        );
    }
}
