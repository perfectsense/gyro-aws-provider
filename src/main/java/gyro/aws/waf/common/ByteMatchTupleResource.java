package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.waf.model.ByteMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.UpdateByteMatchSetRequest;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public abstract class ByteMatchTupleResource extends AbstractWafResource implements Copyable<ByteMatchTuple> {
    private FieldToMatch fieldToMatch;
    private String positionalConstraint;
    private String targetString;
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
        setPositionalConstraint(byteMatchTuple.positionalConstraintAsString());
        setTargetString(byteMatchTuple.targetString().asString(StandardCharsets.UTF_8));
        setTextTransformation(byteMatchTuple.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(byteMatchTuple.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(State state) {
        saveByteMatchTuple(false);
    }

    @Override
    public void update(State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(State state) {
        saveByteMatchTuple(true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("byte match tuple");

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
        StringBuilder sb = new StringBuilder();

        sb.append(getTextTransformation());
        sb.append(" ").append(getPositionalConstraint());
        sb.append(" ").append(getTargetString());

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

    protected abstract void saveByteMatchTuple(boolean isDelete);

    private ByteMatchTuple toByteMatchTuple() {
        return ByteMatchTuple.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .textTransformation(getTextTransformation())
            .positionalConstraint(getPositionalConstraint())
            .targetString(SdkBytes.fromUtf8String(getTargetString()))
            .build();
    }

    protected UpdateByteMatchSetRequest.Builder toByteMatchSetUpdateRequest(boolean isDelete) {
        ByteMatchSetResource parent = (ByteMatchSetResource) parent();

        ByteMatchSetUpdate byteMatchSetUpdate = ByteMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .byteMatchTuple(toByteMatchTuple())
            .build();

        return UpdateByteMatchSetRequest.builder()
            .byteMatchSetId(parent.getId())
            .updates(byteMatchSetUpdate);
    }
}
