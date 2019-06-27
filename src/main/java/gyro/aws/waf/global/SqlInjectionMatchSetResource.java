package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a global sql injection match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::sql-injection-match-set sql-injection-match-set-example
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
@Type("sql-injection-match-set")
public class SqlInjectionMatchSetResource extends gyro.aws.waf.common.SqlInjectionMatchSetResource {
    private Set<SqlInjectionMatchTupleResource> sqlInjectionMatchTuple;

    /**
     * List of sql injection match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.SqlInjectionMatchTupleResource
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

        GetSqlInjectionMatchSetResponse response = getGlobalClient().getSqlInjectionMatchSet(
                r -> r.sqlInjectionMatchSetId(getId())
            );

        this.copyFrom(response.sqlInjectionMatchSet());

        return true;
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateSqlInjectionMatchSetResponse response = client.createSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.sqlInjectionMatchSet().sqlInjectionMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sqlInjectionMatchSetId(getId())
        );
    }
}
