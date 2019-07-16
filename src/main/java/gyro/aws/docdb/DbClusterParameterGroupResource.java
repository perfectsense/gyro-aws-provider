package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterParameterGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBCluster;
import software.amazon.awssdk.services.docdb.model.DBClusterParameterGroup;
import software.amazon.awssdk.services.docdb.model.DbClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DbParameterGroupNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParameterGroupsResponse;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterParametersResponse;
import software.amazon.awssdk.services.docdb.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates an Document db cluster parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::docdb-cluster-param-group db-cluster-param-group-example
 *         db-cluster-param-group-name: "db-cluster-param-group-example"
 *         db-param-group-family: "docdb3.6"
 *         description: "db-cluster-param-group-desc"
 *
 *         tags: {
 *             Name: "db-cluster-param-group-example"
 *         }
 *     end
 */
@Type("docdb-cluster-param-group")
public class DbClusterParameterGroupResource extends DocDbTaggableResource implements Copyable<DBClusterParameterGroup> {

    private String dbClusterParamGroupName;
    private String dbParamGroupFamily;
    private String description;
    private Boolean enableAuditLogs;
    private Boolean enableTls;
    private Boolean enableTtlMonitor;

    //-- Read-only Attributes

    private String arn;

    /**
     * Name of the db cluster parameter group. (Required)
     */
    @Id
    public String getDbClusterParamGroupName() {
        return dbClusterParamGroupName;
    }

    public void setDbClusterParamGroupName(String dbClusterParamGroupName) {
        this.dbClusterParamGroupName = dbClusterParamGroupName;
    }

    /**
     * Name of the db cluster parameter family. (Required)
     */
    public String getDbParamGroupFamily() {
        return dbParamGroupFamily;
    }

    public void setDbParamGroupFamily(String dbParamGroupFamily) {
        this.dbParamGroupFamily = dbParamGroupFamily;
    }

    /**
     * Description for the db cluster parameter family. (Required)
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Enable audit logs. Defaults to false.
     */
    @Updatable
    public Boolean getEnableAuditLogs() {
        if (enableAuditLogs == null) {
            enableAuditLogs = false;
        }

        return enableAuditLogs;
    }

    public void setEnableAuditLogs(Boolean enableAuditLogs) {
        this.enableAuditLogs = enableAuditLogs;
    }

    /**
     * Enable tls. Defaults to true.
     */
    @Updatable
    public Boolean getEnableTls() {
        if (enableTls == null) {
            enableTls = true;
        }

        return enableTls;
    }

    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }

    /**
     * Enable ttl monitor. Defaults to true.
     */
    @Updatable
    public Boolean getEnableTtlMonitor() {
        if (enableTtlMonitor == null) {
            enableTtlMonitor = true;
        }

        return enableTtlMonitor;
    }

    public void setEnableTtlMonitor(Boolean enableTtlMonitor) {
        this.enableTtlMonitor = enableTtlMonitor;
    }

    /**
     * The arn of the db cluster parameter group.
     */
    @Output
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

        DBClusterParameterGroup dbClusterParameterGroup = getDbClusterParameterGroup(client);

        if (dbClusterParameterGroup == null) {
            return false;
        }

        copyFrom(dbClusterParameterGroup);

        return true;
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
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        saveParameters(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        // Check to see if this parameter group is in use. If it's in use and be deleted then
        // wait up to 5 minutes for the deletion to complete.
        List<DBCluster> clusters = client.describeDBClusters().dbClusters();

        boolean using = clusters.stream()
            .anyMatch(r -> r.dbClusterParameterGroup().equals(getDbClusterParamGroupName()) && r.status().equals("available"));

        boolean deleting = clusters.stream()
            .anyMatch(r -> r.dbClusterParameterGroup().equals(getDbClusterParamGroupName()) && r.status().equals("deleting"));

        if (using) {
            throw new GyroException("Unable to delete DB Cluster Parameter Group. Group is in use.");
        }

        if (deleting) {
            Wait.atMost(5, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(
                    () -> client.describeDBClusters().dbClusters().stream()
                        .noneMatch(o -> o.dbClusterParameterGroup().equals(getDbClusterParamGroupName()))
                );
        }

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

    @Override
    public void copyFrom(DBClusterParameterGroup dbClusterParameterGroup) {
        DocDbClient client = createClient(DocDbClient.class);

        setArn(dbClusterParameterGroup.dbClusterParameterGroupArn());
        setDbClusterParamGroupName(dbClusterParameterGroup.dbClusterParameterGroupName());
        setDbParamGroupFamily(dbClusterParameterGroup.dbParameterGroupFamily());
        setDescription(dbClusterParameterGroup.description());

        DescribeDbClusterParametersResponse response = client.describeDBClusterParameters(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
        );

        for (Parameter parameter : response.parameters()) {
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
    }

    private void saveParameters(DocDbClient client) {
        DescribeDbClusterParametersResponse response = client.describeDBClusterParameters(
            r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
        );

        List<Parameter> parameters = new ArrayList<>();

        for (Parameter parameter : response.parameters()) {
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

    private DBClusterParameterGroup getDbClusterParameterGroup(DocDbClient client) {
        DBClusterParameterGroup dbClusterParameterGroup = null;

        if (ObjectUtils.isBlank(getDbClusterParamGroupName())) {
            throw new GyroException("db-cluster-param-group-name is missing, unable to load db cluster parameter group.");
        }

        try{
            DescribeDbClusterParameterGroupsResponse response = client.describeDBClusterParameterGroups(
                r -> r.dbClusterParameterGroupName(getDbClusterParamGroupName())
            );

            if (!response.dbClusterParameterGroups().isEmpty()) {
                dbClusterParameterGroup = response.dbClusterParameterGroups().get(0);
            }

        } catch (DbParameterGroupNotFoundException | DbClusterParameterGroupNotFoundException ex) {

        }

        return dbClusterParameterGroup;
    }

}
