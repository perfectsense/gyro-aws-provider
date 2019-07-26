package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.model.UpdateSqlInjectionMatchSetRequest;

import java.util.Set;

public abstract class SqlInjectionMatchTupleResource extends AbstractWafResource implements Copyable<SqlInjectionMatchTuple> {
    private FieldToMatch fieldToMatch;
    private String textTransformation;

    /**
     * The field setting to match the condition. (Required)
     */
    public FieldToMatch getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatch fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
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
        setTextTransformation(sqlInjectionMatchTuple.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(sqlInjectionMatchTuple.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, Context context) {
        saveSqlInjectionMatchTuple(false);
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, Context context) {
        saveSqlInjectionMatchTuple(true);
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getTextTransformation());

        if (getFieldToMatch() != null) {
            if (!ObjectUtils.isBlank(getFieldToMatch().getData())) {
                sb.append(" ").append(getFieldToMatch().getData());
            }

            if (!ObjectUtils.isBlank(getFieldToMatch().getType())) {
                sb.append(" ").append(getFieldToMatch().getType());
            }
        }

        return sb.toString();
    }

    protected abstract void saveSqlInjectionMatchTuple(boolean isDelete);

    private SqlInjectionMatchTuple toSqlInjectionMatchTuple() {
        return SqlInjectionMatchTuple.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .textTransformation(getTextTransformation())
            .build();
    }

    protected UpdateSqlInjectionMatchSetRequest.Builder toUpdateSqlInjectionMatchSetRequest(boolean isDelete) {
        SqlInjectionMatchSetResource parent = (SqlInjectionMatchSetResource) parent();

        SqlInjectionMatchSetUpdate sqlInjectionMatchSetUpdate = SqlInjectionMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sqlInjectionMatchTuple(toSqlInjectionMatchTuple())
            .build();

        return UpdateSqlInjectionMatchSetRequest.builder()
            .sqlInjectionMatchSetId(parent.getId())
            .updates(sqlInjectionMatchSetUpdate);
    }
}
