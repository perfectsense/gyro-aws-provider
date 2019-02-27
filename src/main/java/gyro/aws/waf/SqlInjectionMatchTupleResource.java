package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;

import java.util.Set;

@ResourceName(parent = "sql-injection-match-set", value = "sql-injection-match-tuple")
public class SqlInjectionMatchTupleResource extends AwsResource {
    private String data;
    private String type;
    private String textTransformation;

    /**
     * If type selected as ```HEADER``` or ```SINGLE_QUERY_ARG```, the value needs to be provided.
     */
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Part of the request to filter on. Valid values ```URI```, ```QUERY_STRING```, ```HEADER```, ```METHOD```, ```BODY```, ```SINGLE_QUERY_ARG```, ```ALL_QUERY_ARGS```. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Text transformation on the data provided before doing the check. Valid values ``NONE``, ``COMPRESS_WHITE_SPACE``, ``HTML_ENTITY_DECODE``, ``LOWERCASE``, ``CMD_LINE``, ``URL_DECODE``. (Required)
     */
    public String getTextTransformation() {
        return textTransformation != null ? textTransformation.toUpperCase() : null;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

    public SqlInjectionMatchTupleResource() {

    }

    public SqlInjectionMatchTupleResource(SqlInjectionMatchTuple sqlInjectionMatchTuple) {
        setData(sqlInjectionMatchTuple.fieldToMatch().data());
        setType(sqlInjectionMatchTuple.fieldToMatch().typeAsString());
        setTextTransformation(sqlInjectionMatchTuple.textTransformationAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveSqlInjectionMatchTuple(client, getSqlInjectionMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveSqlInjectionMatchTuple(client, getSqlInjectionMatchTuple(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("sql injection match tuple");

        if (!ObjectUtils.isBlank(getData())) {
            sb.append(" - ").append(getData());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s", getData(), getType(), getTextTransformation());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private SqlInjectionMatchTuple getSqlInjectionMatchTuple() {
        return SqlInjectionMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .build();
    }

    private void saveSqlInjectionMatchTuple(WafClient client, SqlInjectionMatchTuple sqlInjectionMatchTuple, boolean isDelete) {
        SqlInjectionMatchSetResource parent = (SqlInjectionMatchSetResource) parent();

        SqlInjectionMatchSetUpdate sqlInjectionMatchSetUpdate = SqlInjectionMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sqlInjectionMatchTuple(sqlInjectionMatchTuple)
            .build();

        client.updateSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sqlInjectionMatchSetId(parent.getSqlInjectionMatchSetId())
                .updates(sqlInjectionMatchSetUpdate)
        );
    }
}
