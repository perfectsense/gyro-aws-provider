package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.waf.model.ByteMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.UpdateByteMatchSetRequest;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public abstract class ByteMatchTupleResource extends AbstractWafResource implements Copyable<ByteMatchTuple> {
    private String type;
    private String data;
    private String positionalConstraint;
    private String targetString;
    private String textTransformation;

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
     * If type selected as ``HEADER`` or ``SINGLE_QUERY_ARG``, the value needs to be provided.
     */
    public String getData() {
        return data != null ? data.toLowerCase() : null;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * The comparison to be done on the filter. Valid values are ``EXACTLY`` or ``STARTS_WITH`` or ``ENDS_WITH`` or ``CONTAINS`` or ``CONTAINS_WORD``. (Required)
     */
    public String getPositionalConstraint() {
        return positionalConstraint != null ? positionalConstraint.toUpperCase() : null;
    }

    public void setPositionalConstraint(String positionalConstraint) {
        this.positionalConstraint = positionalConstraint;
    }

    /**
     * The target string to filter on for the byte match filter. (Required)
     */
    public String getTargetString() {
        return targetString;
    }

    public void setTargetString(String targetString) {
        this.targetString = targetString;
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
    public void copyFrom(ByteMatchTuple byteMatchTuple) {
        setType(byteMatchTuple.fieldToMatch().typeAsString());
        setData(byteMatchTuple.fieldToMatch().data());
        setPositionalConstraint(byteMatchTuple.positionalConstraintAsString());
        setTargetString(byteMatchTuple.targetString().asString(StandardCharsets.UTF_8));
        setTextTransformation(byteMatchTuple.textTransformationAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveByteMatchTuple(toByteMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveByteMatchTuple(toByteMatchTuple(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("byte match tuple");

        if (!ObjectUtils.isBlank(getData())) {
            sb.append(" - ").append(getData());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        if (!ObjectUtils.isBlank(getPositionalConstraint())) {
            sb.append(" - ").append(getPositionalConstraint());
        }

        if (!ObjectUtils.isBlank(getTargetString())) {
            sb.append(" - ").append(getTargetString());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s %s %s", getData(), getType(), getTextTransformation(), getPositionalConstraint(), getTargetString());
    }

    protected abstract void saveByteMatchTuple(ByteMatchTuple byteMatchTuple, boolean isDelete);

    private ByteMatchTuple toByteMatchTuple() {
        return ByteMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .positionalConstraint(getPositionalConstraint())
            .targetString(SdkBytes.fromUtf8String(getTargetString()))
            .build();
    }

    protected UpdateByteMatchSetRequest.Builder toByteMatchSetUpdateRequest(ByteMatchTuple byteMatchTuple, boolean isDelete) {
        ByteMatchSetResource parent = (ByteMatchSetResource) parent();

        ByteMatchSetUpdate byteMatchSetUpdate = ByteMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .byteMatchTuple(byteMatchTuple)
            .build();

        return UpdateByteMatchSetRequest.builder()
            .byteMatchSetId(parent.getId())
            .updates(byteMatchSetUpdate);
    }
}
