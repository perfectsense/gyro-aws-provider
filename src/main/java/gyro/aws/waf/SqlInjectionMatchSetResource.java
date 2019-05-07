package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceType("sql-injection-match-set")
public class SqlInjectionMatchSetResource extends AwsResource {
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

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetSqlInjectionMatchSetResponse response = client.getSqlInjectionMatchSet(
            r -> r.sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
        );

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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateSqlInjectionMatchSetResponse response = client.createSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        SqlInjectionMatchSet sqlInjectionMatchSet = response.sqlInjectionMatchSet();
        setSqlInjectionMatchSetId(sqlInjectionMatchSet.sqlInjectionMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
        );
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
