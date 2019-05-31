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
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveXssMatchTuple(getXssMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveXssMatchTuple(getXssMatchTuple(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("xss match tuple");

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
    public void copyFrom(XssMatchTuple xssMatchTuple) {
        setData(xssMatchTuple.fieldToMatch().data());
        setType(xssMatchTuple.fieldToMatch().typeAsString());
        setTextTransformation(xssMatchTuple.textTransformationAsString());
    }

    protected abstract void saveXssMatchTuple(XssMatchTuple xssMatchTuple, boolean isDelete);

    private XssMatchTuple getXssMatchTuple() {
        return XssMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .build();
    }

    protected UpdateXssMatchSetRequest.Builder getUpdateXssMatchSetRequest(XssMatchTuple xssMatchTuple, boolean isDelete) {
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
