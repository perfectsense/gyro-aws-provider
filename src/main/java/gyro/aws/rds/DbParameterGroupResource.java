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
import software.amazon.awssdk.services.rds.model.CreateDbParameterGroupResponse;
import software.amazon.awssdk.services.rds.model.DBParameterGroup;
import software.amazon.awssdk.services.rds.model.DbParameterGroupNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbParameterGroupsResponse;
import software.amazon.awssdk.services.rds.model.Parameter;
import software.amazon.awssdk.services.rds.paginators.DescribeDBParametersIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a db parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-parameter-group parameter-group
 *        name: "parameter-group-example"
 *        description: "some description"
 *        family: "mysql5.6"
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
 *            Name: "db-parameter-group-example"
 *        }
 *    end
 */
@Type("db-parameter-group")
public class DbParameterGroupResource extends RdsTaggableResource implements Copyable<DBParameterGroup> {

    public static final String AWS_ARN_RESOURCE_TYPE = "pg";

    private String description;
    private String family;
    private String name;
    private List<DbParameter> parameter;

    /**
     * The description of the DB parameter group.
     */
    @Required
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the DB parameter group family.
     */
    @Required
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * The name of the DB parameter group.
     */
    @Required
    public String getName() {
        if (name == null && getArn() != null) {
            name = getNameFromArn();
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of DB parameters.
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
    public void copyFrom(DBParameterGroup group) {
        setFamily(group.dbParameterGroupFamily());
        setName(group.dbParameterGroupName());
        setDescription(group.description());
        setArn(group.dbParameterGroupArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db parameter group.");
        }

        try {
            DescribeDbParameterGroupsResponse response = client.describeDBParameterGroups(
                r -> r.dbParameterGroupName(getName())
            );

            response.dbParameterGroups().forEach(this::copyFrom);

            DescribeDBParametersIterable iterable = client.describeDBParametersPaginator(
                r -> r.dbParameterGroupName(getName())
            );

            Set<String> names = getParameter().stream().map(DbParameter::getName).collect(Collectors.toSet());
            getParameter().clear();
            iterable.stream().forEach(
                r -> getParameter().addAll(r.parameters().stream()
                    .filter(p -> names.contains(p.parameterName()))
                    .map(p -> {
                                DbParameter parameter = new DbParameter();
                                parameter.setApplyMethod(p.applyMethodAsString());
                                parameter.setName(p.parameterName());
                                parameter.setValue(p.parameterValue());
                                return parameter;
                            }
                        )
                    .collect(Collectors.toList())
                )
            );

        } catch (DbParameterGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateDbParameterGroupResponse response = client.createDBParameterGroup(
            r -> r.dbParameterGroupFamily(getFamily())
                    .dbParameterGroupName(getName())
                    .description(getDescription())
        );

        setArn(response.dbParameterGroup().dbParameterGroupArn());

        if (!getParameter().isEmpty()) {
            List<DbParameter> dbParameters = new ArrayList<>(getParameter());
            getParameter().clear();
            state.save();
            setParameter(dbParameters);

            modifyParameterGroup();
        }
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        modifyParameterGroup();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBParameterGroup(r -> r.dbParameterGroupName(getName()));
    }

    private void modifyParameterGroup() {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBParameterGroup(
            r -> r.dbParameterGroupName(getName())
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
