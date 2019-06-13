package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.model.UpdateSqlInjectionMatchSetRequest;

import java.util.Set;

public abstract class SqlInjectionMatchTupleResource extends AbstractWafResource implements Copyable<SqlInjectionMatchTuple> {
    private String data;
    private String type;
    private String textTransformation;

    /**
     * If type selected as ``HEADER`` or ``SINGLE_QUERY_ARG``, the value needs to be provided.
     */
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Part of the request to filter on. Valid values are ``URI`` or ``QUERY_STRING`` or ``HEADER`` or ``METHOD`` or ``BODY`` or ``SINGLE_QUERY_ARG`` or ``ALL_QUERY_ARGS``. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Text transformation on the data provided before doing the check. Valid values are ``NONE`` or ``COMPRESS_WHITE_SPACE`` or ``HTML_ENTITY_DECODE`` or ``LOWERCASE`` or ``CMD_LINE`` or ``URL_DECODE``. (Required)
     */
    public String getTextTransformation() {
        return textTransformation != null ? textTransformation.toUpperCase() : null;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

    @Override
    public void copyFrom(SqlInjectionMatchTuple sqlInjectionMatchTuple) {
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
        saveSqlInjectionMatchTuple(getSqlInjectionMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveSqlInjectionMatchTuple(getSqlInjectionMatchTuple(), true);
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

    protected abstract void saveSqlInjectionMatchTuple(SqlInjectionMatchTuple sqlInjectionMatchTuple, boolean isDelete);

    private SqlInjectionMatchTuple getSqlInjectionMatchTuple() {
        return SqlInjectionMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .build();
    }

    protected UpdateSqlInjectionMatchSetRequest.Builder getUpdateSqlInjectionMatchSetRequest(SqlInjectionMatchTuple sqlInjectionMatchTuple, boolean isDelete) {
        SqlInjectionMatchSetResource parent = (SqlInjectionMatchSetResource) parent();

        SqlInjectionMatchSetUpdate sqlInjectionMatchSetUpdate = SqlInjectionMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sqlInjectionMatchTuple(sqlInjectionMatchTuple)
            .build();

        return UpdateSqlInjectionMatchSetRequest.builder()
            .sqlInjectionMatchSetId(parent.getId())
            .updates(sqlInjectionMatchSetUpdate);
    }
}
