/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbParameterGroupResponse;
import software.amazon.awssdk.services.neptune.model.DBParameterGroup;
import software.amazon.awssdk.services.neptune.model.DbParameterGroupNotFoundException;
import software.amazon.awssdk.services.neptune.model.DescribeDbParameterGroupsResponse;
import software.amazon.awssdk.services.neptune.model.DescribeDbParametersResponse;
import software.amazon.awssdk.services.neptune.model.DescribeEngineDefaultParametersResponse;
import software.amazon.awssdk.services.neptune.model.Parameter;

/**
 * Create a Neptune parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *   aws::neptune-parameter-group neptune-parameter-group-example
 *       name: "neptune-parameter-group-example"
 *       description: "neptune parameter group example description"
 *       family: "neptune1"
 *
 *       query-timeout
 *           value: "120000"
 *       end
 *
 *       tags: {
 *           Name: "neptune-parameter-group-example"
 *       }
 *
 *   end
 */
@Type("neptune-parameter-group")
public class NeptuneParameterGroupResource extends NeptuneTaggableResource implements Copyable<DBParameterGroup> {

    private String description;
    private String family;
    private String name;
    private NeptuneParameter queryTimeout;

    /**
     * The description of the parameter group.
     */
    @Required
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the parameter group family.
     */
    @ValidStrings("neptune1")
    @Required
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * The name of the parameter group.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Graph query timeout (ms).
     * The ``value`` field of this ``NeptuneParameter`` must be an integer from ``10`` to ``2147483647``, and defaults to ``120000``.
     *
     * @subresource gyro.aws.neptune.NeptuneParameter
     */
    @Updatable
    public NeptuneParameter getQueryTimeout() {
        if (queryTimeout != null) {
            queryTimeout.setName("neptune_query_timeout");
            queryTimeout.setApplyMethod("pending-reboot");
        }

        return queryTimeout;
    }

    public void setQueryTimeout(NeptuneParameter queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @Override
    public void copyFrom(DBParameterGroup model) {
        setFamily(model.dbParameterGroupFamily());
        setName(model.dbParameterGroupName());
        setDescription(model.description());
        setArn(model.dbParameterGroupArn());
        loadParameters();
    }

    @Override
    protected boolean doRefresh() {
        DBParameterGroup group = getDbParameterGroup();

        if (group == null) {
            return false;
        }

        copyFrom(group);
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);
        CreateDbParameterGroupResponse response = client.createDBParameterGroup(
            r -> r.dbParameterGroupName(getName())
                .dbParameterGroupFamily(getFamily())
                .description(getDescription())
        );

        setArn(response.dbParameterGroup().dbParameterGroupArn());
        state.save();
        saveParameters();
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        saveParameters();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);
        client.deleteDBParameterGroup(r -> r.dbParameterGroupName(getName()));
    }

    private DBParameterGroup getDbParameterGroup() {
        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load parameter group.");
        }

        NeptuneClient client = createClient(NeptuneClient.class);

        DBParameterGroup group = null;

        try {
            DescribeDbParameterGroupsResponse parameterGroupsResponse = client.describeDBParameterGroups(r -> r
                .dbParameterGroupName(getName())
            );

            if (parameterGroupsResponse.hasDbParameterGroups()) {
                group = parameterGroupsResponse.dbParameterGroups().get(0);
            }
        } catch (DbParameterGroupNotFoundException ex) {
            //  group not found - ignore exception and return null
        }

        return group;
    }

    private void loadParameters() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DescribeDbParametersResponse parametersResponse = client.describeDBParameters(
            r -> r.dbParameterGroupName(getName())
        );

        setQueryTimeout(null);

        if (parametersResponse.hasParameters()) {
            Parameter param = parametersResponse.parameters().get(0);
            if (param.parameterName().equals("neptune_query_timeout")) {
                NeptuneParameter copiedParam = new NeptuneParameter();
                copiedParam.copyFrom(param);
                setQueryTimeout(copiedParam);
            }
        }
    }

    private void saveParameters() {
        NeptuneClient client = createClient(NeptuneClient.class);

        Parameter parameter = getQueryTimeout() != null
            ? getQueryTimeout().toParameter()
            : getDefaultParameter(client, getFamily());

        client.modifyDBParameterGroup(r -> r.dbParameterGroupName(getName()).parameters(parameter));
    }

    private Parameter getDefaultParameter(NeptuneClient client, String engineFamily) {
        DescribeEngineDefaultParametersResponse response = client.describeEngineDefaultParameters(
            r -> r.dbParameterGroupFamily(engineFamily)
        );

        Parameter defaultQueryTimeout = null;

        if (response.engineDefaults().hasParameters()) {
            Parameter defaultParameter = response.engineDefaults().parameters().get(0);

            defaultQueryTimeout = Parameter.builder()
                .parameterName(defaultParameter.parameterName())
                .parameterValue(defaultParameter.parameterValue())
                .allowedValues(defaultParameter.allowedValues())
                .applyType(defaultParameter.applyType())
                .dataType(defaultParameter.dataType())
                .description(defaultParameter.description())
                .isModifiable(defaultParameter.isModifiable())
                .minimumEngineVersion(defaultParameter.minimumEngineVersion())
                .source(defaultParameter.source())
                .applyMethod("pending-reboot")
                .build();
        }

        return defaultQueryTimeout;
    }
}
