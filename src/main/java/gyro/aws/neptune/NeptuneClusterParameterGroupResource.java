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
import software.amazon.awssdk.services.neptune.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Create a Neptune cluster parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::neptune-cluster-parameter-group neptune-cluster-parameter-group
 *        name: "neptune-cluster-parameter-group-example"
 *        description: "neptune cluter parameter group example description"
 *        family: "neptune1"
 *        tags: {
 *            Name: "neptune-cluster-parameter-group-example-tag"
 *        }
 *
 *        enable-audit-log
 *            value: "0"
 *        end
 *
 *        enforce-ssl
 *            value: "1"
 *        end
 *
 *        lab-mode
 *            value: "Streams=enabled, ReadWriteConflictDetection=disabled"
 *        end
 *
 *        query-timeout
 *            value: "120000"
 *        end
 *    end
 */
@Type("neptune-cluster-parameter-group")
public class NeptuneClusterParameterGroupResource extends NeptuneTaggableResource implements Copyable<DBClusterParameterGroup> {

    private String description;
    private String family;
    private String name;
    private NeptuneParameter enableAuditLog;
    private NeptuneParameter enforceSsl;
    private NeptuneParameter labMode;
    private NeptuneParameter queryTimeout;

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
     * The only supported family for Neptune is ``neptune1``
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
     * The name of the cluster parameter group. (Required)
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
     * Enable audit logs. Valid values are 0 and 1.
     * Value defaults to 0.
     *
     * @subresource gyro.aws.neptune.NeptuneParameter
     */
    @Updatable
    public NeptuneParameter getEnableAuditLog() {
        if (enableAuditLog != null) {
            enableAuditLog.setName("neptune_enable_audit_log");
            enableAuditLog.setApplyMethod("pending-reboot");
        }
        return enableAuditLog;
    }

    public void setEnableAuditLog(NeptuneParameter enableAuditLog) {
        this.enableAuditLog = enableAuditLog;
    }

    /**
     * Accept SSL/TLS connections only. Valid values are 0 and 1.
     * Value defaults to 1.
     *
     * @subresource gyro.aws.neptune.NeptuneParameter
     */
    @Updatable
    public NeptuneParameter getEnforceSsl() {
        if (enforceSsl != null) {
            enforceSsl.setName("neptune_enforce_ssl");
            enforceSsl.setApplyMethod("pending-reboot");
        }
        return enforceSsl;
    }

    public void setEnforceSsl(NeptuneParameter enforceSsl) {
        this.enforceSsl = enforceSsl;
    }

    /**
     * Toggle Neptune engine experimental features.
     * Value is a comma-separated list including ``(feature name)=enabled`` or ``(feature name)=disabled``.
     * Valid feature names include ``ObjectIndex``, ``Streams``, and ``ReadWriteConflictDetection``.
     *
     * @subresource gyro.aws.neptune.NeptuneParameter
     */
    @Updatable
    public NeptuneParameter getLabMode() {
        if (labMode != null) {
            labMode.setName("neptune_lab_mode");
            labMode.setApplyMethod("pending-reboot");
        }
        return labMode;
    }

    public void setLabMode(NeptuneParameter labMode) {
        this.labMode = labMode;
    }

    /**
     * Graph query timeout (ms). Valid values range from 10 to 2147483647.
     * Value defaults to 120000.
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
    public void copyFrom(DBClusterParameterGroup model) {
        setFamily(model.dbParameterGroupFamily());
        setName(model.dbClusterParameterGroupName());
        setDescription(model.description());
        setArn(model.dbClusterParameterGroupArn());
        loadParameters();
    }

    @Override
    protected boolean doRefresh() {
        DBClusterParameterGroup group = getDbClusterParameterGroup();
        if (group == null) {
            return false;
        }

        copyFrom(group);
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);
        CreateDbClusterParameterGroupResponse createResponse = client.createDBClusterParameterGroup(
            r -> r.dbClusterParameterGroupName(getName())
                .dbParameterGroupFamily(getFamily())
                .description(getDescription())
        );

        setArn(createResponse.dbClusterParameterGroup().dbClusterParameterGroupArn());
        saveParameters();
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        saveParameters();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);
        client.deleteDBClusterParameterGroup(r -> r.dbClusterParameterGroupName(getName()));
    }

    private DBClusterParameterGroup getDbClusterParameterGroup() {
        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load cluster parameter group.");
        }

        NeptuneClient client = createClient(NeptuneClient.class);
        DBClusterParameterGroup group = null;

        try {
            DescribeDbClusterParameterGroupsResponse parameterGroupsResponse = client.describeDBClusterParameterGroups(
                r -> r.dbClusterParameterGroupName(getName())
            );

            if (parameterGroupsResponse.hasDbClusterParameterGroups()) {
                group = parameterGroupsResponse.dbClusterParameterGroups().get(0);
            }
        } catch (DbClusterParameterGroupNotFoundException ex) {
            //  group not found - ignore exception and return null
        }

        return group;
    }

    private void loadParameters() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DescribeDbClusterParametersResponse parametersResponse = client.describeDBClusterParameters(
            r -> r.dbClusterParameterGroupName(getName())
        );

        setEnableAuditLog(null);
        setEnforceSsl(null);
        setLabMode(null);
        setQueryTimeout(null);

        if (parametersResponse.hasParameters()) {
            parametersResponse.parameters().stream().forEach(p -> {
                NeptuneParameter param = new NeptuneParameter();
                param.copyFrom(p);
                switch (param.getName()) {
                    case "neptune_enable_audit_log":
                        setEnableAuditLog(param);
                        break;
                    case "neptune_enforce_ssl":
                        setEnforceSsl(param);
                        break;
                    case "neptune_lab_mode":
                        setLabMode(param);
                        break;
                    case "neptune_query_timeout":
                        setQueryTimeout(param);
                        break;
                    default:
                        break;
                }
            });

        }
    }

    private void saveParameters() {
        NeptuneClient client = createClient(NeptuneClient.class);

        List<Parameter> parameters = new ArrayList<>();
        List<NeptuneParameter> defaultParameters = null;

        if (getEnableAuditLog() != null) {
            parameters.add(getEnableAuditLog().toParameter());
        } else {
            defaultParameters = getDefaultClusterParameters(client, getFamily());
            for (NeptuneParameter p : defaultParameters) {
                if (p.getName().equals("neptune_enable_audit_log")) {
                    parameters.add(p.toParameter());
                    break;
                }
            }
        }
        if (getEnforceSsl() != null) {
            parameters.add(getEnforceSsl().toParameter());
        } else {
            if (defaultParameters == null) {
                defaultParameters = getDefaultClusterParameters(client, getFamily());
            }
            for (NeptuneParameter p : defaultParameters) {
                if (p.getName().equals("neptune_enforce_ssl")) {
                    parameters.add(p.toParameter());
                    break;
                }
            }
        }
        if (getLabMode() != null) {
            parameters.add(getLabMode().toParameter());
        } else {
            if (defaultParameters == null) {
                defaultParameters = getDefaultClusterParameters(client, getFamily());
            }
            for (NeptuneParameter p : defaultParameters) {
                if (p.getName().equals("neptune_lab_mode")) {
                    parameters.add(p.toParameter());
                    break;
                }
            }
        }
        if (getQueryTimeout() != null) {
            parameters.add(getQueryTimeout().toParameter());
        } else {
            if (defaultParameters == null) {
                defaultParameters = getDefaultClusterParameters(client, getFamily());
            }
            for (NeptuneParameter p : defaultParameters) {
                if (p.getName().equals("neptune_query_timeout")) {
                    parameters.add(p.toParameter());
                    break;
                }
            }
        }

        client.modifyDBClusterParameterGroup(r -> r.dbClusterParameterGroupName(getName()).parameters(parameters));
    }

    static List<NeptuneParameter> getDefaultClusterParameters(NeptuneClient client, String engineFamily) {
        List<NeptuneParameter> defaultParameters = new ArrayList<>();

        DescribeEngineDefaultClusterParametersResponse response = client.describeEngineDefaultClusterParameters(
            r -> r.dbParameterGroupFamily(engineFamily)
        );

        if (response.engineDefaults().hasParameters()) {
            response.engineDefaults().parameters().stream().forEach(p -> {
                NeptuneParameter param = new NeptuneParameter();
                param.copyFrom(p);
                param.setApplyMethod("pending-reboot");
                defaultParameters.add(param);
            });
        }
        NeptuneParameter defaultQueryTimeout = NeptuneParameterGroupResource.getDefaultParameter(client, engineFamily);
        defaultParameters.add(defaultQueryTimeout);

        return defaultParameters;
    }
}
