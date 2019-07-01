package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a size injection match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-sql-injection-match-set-regional sql-injection-match-set-example
 *     name: "sql-injection-match-set-example"
 *
 *     sql-injection-match-tuple
 *         field-to-match
 *             type: "METHOD"
 *         end
 *         text-transformation: "NONE"
 *     end
 * end
 */
@Type("waf-sql-injection-match-set-regional")
public class SqlInjectionMatchSetResource extends gyro.aws.waf.common.SqlInjectionMatchSetResource {
    private Set<SqlInjectionMatchTupleResource> sqlInjectionMatchTuple;

    /**
     * List of sql injection match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.SqlInjectionMatchTupleResource
     */
    @Updatable
    public Set<SqlInjectionMatchTupleResource> getSqlInjectionMatchTuple() {
        if (sqlInjectionMatchTuple == null) {
            sqlInjectionMatchTuple = new HashSet<>();
        }

        return sqlInjectionMatchTuple;
    }

    public void setSqlInjectionMatchTuple(Set<SqlInjectionMatchTupleResource> sqlInjectionMatchTuple) {
        this.sqlInjectionMatchTuple = sqlInjectionMatchTuple;

        if (sqlInjectionMatchTuple.size() > 10) {
            throw new GyroException("Sql Injection Match Tuple limit exception. Max 10 per Byte Match Set.");
        }
    }

    @Override
    public void copyFrom(SqlInjectionMatchSet sqlInjectionMatchSet) {
        setId(sqlInjectionMatchSet.sqlInjectionMatchSetId());
        setName(sqlInjectionMatchSet.name());

        getSqlInjectionMatchTuple().clear();
        for (SqlInjectionMatchTuple sqlInjectionMatchTuple : sqlInjectionMatchSet.sqlInjectionMatchTuples()) {
            SqlInjectionMatchTupleResource sqlInjectionMatchTupleResource = newSubresource(SqlInjectionMatchTupleResource.class);
            sqlInjectionMatchTupleResource.copyFrom(sqlInjectionMatchTuple);
            getSqlInjectionMatchTuple().add(sqlInjectionMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetSqlInjectionMatchSetResponse response = getRegionalClient().getSqlInjectionMatchSet(
            r -> r.sqlInjectionMatchSetId(getId())
        );

        this.copyFrom(response.sqlInjectionMatchSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateSqlInjectionMatchSetResponse response = client.createSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.sqlInjectionMatchSet().sqlInjectionMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sqlInjectionMatchSetId(getId())
        );
    }

    SqlInjectionMatchSet getSqlInjectionMatchSet(WafRegionalClient client) {
        return client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(getId())).sqlInjectionMatchSet();
    }
}
