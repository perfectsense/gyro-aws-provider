package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.UpdateXssMatchSetRequest;
import software.amazon.awssdk.services.waf.model.XssMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

import java.util.Set;

public abstract class XssMatchTupleResource extends AbstractWafResource implements Copyable<XssMatchTuple> {
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
    public void copyFrom(XssMatchTuple xssMatchTuple) {
        setTextTransformation(xssMatchTuple.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(xssMatchTuple.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveXssMatchTuple(toXssMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveXssMatchTuple(toXssMatchTuple(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("xss match tuple");

        if (getFieldToMatch() != null) {
            if (!ObjectUtils.isBlank(getFieldToMatch().getData())) {
                sb.append(" - ").append(getFieldToMatch().getData());
            }

            if (!ObjectUtils.isBlank(getFieldToMatch().getType())) {
                sb.append(" - ").append(getFieldToMatch().getType());
            }
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        return sb.toString();
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

    protected abstract void saveXssMatchTuple(XssMatchTuple xssMatchTuple, boolean isDelete);

    private XssMatchTuple toXssMatchTuple() {
        return XssMatchTuple.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .textTransformation(getTextTransformation())
            .build();
    }

    protected UpdateXssMatchSetRequest.Builder toUpdateXssMatchSetRequest(XssMatchTuple xssMatchTuple, boolean isDelete) {
        XssMatchSetResource parent = (XssMatchSetResource) parent();

        XssMatchSetUpdate xssMatchSetUpdate = XssMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .xssMatchTuple(xssMatchTuple)
            .build();

        return UpdateXssMatchSetRequest.builder()
            .xssMatchSetId(parent.getId())
            .updates(xssMatchSetUpdate);
    }
}
