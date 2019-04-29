package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterParameterGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParameterGroupsResponse;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParametersResponse;
import software.amazon.awssdk.services.docdb.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("db-cluster-param-group")
public class DbClusterParameterGroupResource extends DocDbTaggableResource {
    private String dbClusterParamGroupName;
    private String dbParamGroupFamily;
    private String description;
    private Boolean enableAuditLogs;
    private Boolean enableTls;
    private Boolean enableTtlMonitor;

    private String arn;

    public String getDbClusterParamGroupName() {
        return dbClusterParamGroupName;
    }

    public void setDbClusterParamGroupName(String dbClusterParamGroupName) {
        this.dbClusterParamGroupName = dbClusterParamGroupName;
    }

    public String getDbParamGroupFamily() {
        return dbParamGroupFamily;
    }

    public void setDbParamGroupFamily(String dbParamGroupFamily) {
        this.dbParamGroupFamily = dbParamGroupFamily;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableAuditLogs() {
        if (enableAuditLogs == null) {
            enableAuditLogs = false;
        }

        return enableAuditLogs;
    }

    public void setEnableAuditLogs(Boolean enableAuditLogs) {
        this.enableAuditLogs = enableAuditLogs;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableTls() {
        if (enableTls == null) {
            enableTls = true;
        }

        return enableTls;
    }

    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }

    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableTtlMonitor() {
        if (enableTtlMonitor == null) {
            enableTtlMonitor = true;
        }

        return enableTtlMonitor;
    }

    public void setEnableTtlMonitor(Boolean enableTtlMonitor) {
        this.enableTtlMonitor = enableTtlMonitor;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected String getId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DescribeDbClusterParameterGroupsResponse response = client.describeDBClusterParameterGroups(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
        );

        if (!response.dbClusterParameterGroups().isEmpty()) {
            DBClusterParameterGroup dbClusterParameterGroup = response.dbClusterParameterGroups().get(0);
            setArn(dbClusterParameterGroup.dbClusterParameterGroupArn());
            setDbParamGroupFamily(dbClusterParameterGroup.dbParameterGroupFamily());
            setDescription(dbClusterParameterGroup.description());

            DescribeDbClusterParametersResponse response1 = client.describeDBClusterParameters(
                r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
            );

            for (Parameter parameter : response1.parameters()) {
                switch (parameter.parameterName()) {
                    case "audit_logs":
                        setEnableAuditLogs(parameter.parameterValue().equalsIgnoreCase("enabled"));
                        break;
                    case "tls":
                        setEnableTls(parameter.parameterValue().equalsIgnoreCase("enabled"));
                        break;
                    case "ttl_monitor":
                        setEnableTtlMonitor(parameter.parameterValue().equalsIgnoreCase("enabled"));
                        break;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterParameterGroupResponse response = client.createDBClusterParameterGroup(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
                .dbParameterGroupFamily(getDbParamGroupFamily())
                .description(getDescription())
        );

        setArn(response.dbClusterParameterGroup().dbClusterParameterGroupArn());

        if (getEnableAuditLogs() || !getEnableTls() || !getEnableTtlMonitor()) {
            saveParameters(client);
        }
    }@Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        saveParameters(client);
    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBClusterParameterGroup(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db cluster parameter group");

        if (!ObjectUtils.isBlank(getDbClusterParamGroupName())) {
            sb.append(" - ").append(getDbClusterParamGroupName());
        }

        return sb.toString();
    }

    private void saveParameters(DocDbClient client) {
        DescribeDbClusterParametersResponse response1 = client.describeDBClusterParameters(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
        );

        List<Parameter> parameters = new ArrayList<>();

        for (Parameter parameter : response1.parameters()) {
            switch (parameter.parameterName()) {
                case "audit_logs":
                    parameters.add(getModifiedParam(parameter, getEnableAuditLogs()));
                    break;
                case "tls":
                    parameters.add(getModifiedParam(parameter, getEnableTls()));
                    break;
                case "ttl_monitor":
                    parameters.add(getModifiedParam(parameter, getEnableTtlMonitor()));
                    break;
                default:
                    parameters.add(parameter);
                    break;
            }
        }

        client.modifyDBClusterParameterGroup(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
                .parameters(parameters)
        );
    }

    private Parameter getModifiedParam(Parameter parameter, boolean isEnabled) {
        return Parameter.builder()
            .allowedValues(parameter.allowedValues())
            .applyMethod(parameter.applyMethod())
            .applyType(parameter.applyType())
            .dataType(parameter.dataType())
            .isModifiable(parameter.isModifiable())
            .minimumEngineVersion(parameter.minimumEngineVersion())
            .parameterName(parameter.parameterName())
            .parameterValue(isEnabled ? "enabled" : "disabled")
            .source(parameter.source())
            .build();
    }
}
