package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("sql-injection-match-set")
public class SqlInjectionMatchSetResource extends AbstractWafResource {
    private String name;
    private String sqlInjectionMatchSetId;
    private List<SqlInjectionMatchTupleResource> sqlInjectionMatchTuple;

    /**
     * The name of the sql injection match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getSqlInjectionMatchSetId() {
        return sqlInjectionMatchSetId;
    }

    public void setSqlInjectionMatchSetId(String sqlInjectionMatchSetId) {
        this.sqlInjectionMatchSetId = sqlInjectionMatchSetId;
    }

    /**
     * List of sql injection match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.SqlInjectionMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<SqlInjectionMatchTupleResource> getSqlInjectionMatchTuple() {
        if (sqlInjectionMatchTuple == null) {
            sqlInjectionMatchTuple = new ArrayList<>();
        }

        return sqlInjectionMatchTuple;
    }

    public void setSqlInjectionMatchTuple(List<SqlInjectionMatchTupleResource> sqlInjectionMatchTuple) {
        this.sqlInjectionMatchTuple = sqlInjectionMatchTuple;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getSqlInjectionMatchSetId())) {
            return false;
        }

        GetSqlInjectionMatchSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getSqlInjectionMatchSet(
                r -> r.sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
            );
        } else {
            response = getGlobalClient().getSqlInjectionMatchSet(
                r -> r.sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
            );
        }

        SqlInjectionMatchSet sqlInjectionMatchSet = response.sqlInjectionMatchSet();
        setName(sqlInjectionMatchSet.name());

        getSqlInjectionMatchTuple().clear();
        for (SqlInjectionMatchTuple sqlInjectionMatchTuple : sqlInjectionMatchSet.sqlInjectionMatchTuples()) {
            SqlInjectionMatchTupleResource sqlInjectionMatchTupleResource = new SqlInjectionMatchTupleResource(sqlInjectionMatchTuple);
            sqlInjectionMatchTupleResource.parent(this);
            getSqlInjectionMatchTuple().add(sqlInjectionMatchTupleResource);
        }
        return true;
    }

    @Override
    public void create() {
        CreateSqlInjectionMatchSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createSqlInjectionMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createSqlInjectionMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        SqlInjectionMatchSet sqlInjectionMatchSet = response.sqlInjectionMatchSet();
        setSqlInjectionMatchSetId(sqlInjectionMatchSet.sqlInjectionMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteSqlInjectionMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteSqlInjectionMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
            );
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("sql injection match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getSqlInjectionMatchSetId())) {
            sb.append(" - ").append(getSqlInjectionMatchSetId());
        }

        return sb.toString();
    }
}
