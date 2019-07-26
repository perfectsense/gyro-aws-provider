package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateSizeConstraintSetRequest;

import java.util.Set;

public abstract class SizeConstraintResource extends AbstractWafResource implements Copyable<SizeConstraint> {
    private FieldToMatch fieldToMatch;
    private String comparisonOperator;
    private String textTransformation;
    private Long size;

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
     * The comparison to be done on the filter. Valid values are ``EQ`` or ``NE`` or ``LE`` or ``LT`` or ``GE`` or ``GT``. (Required)
     */
    public String getComparisonOperator() {
        return comparisonOperator != null ? comparisonOperator.toUpperCase() : null;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
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

    /**
     * Size of the request in integers.
     */
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public void copyFrom(SizeConstraint sizeConstraint) {
        setComparisonOperator(sizeConstraint.comparisonOperatorAsString());
        setSize(sizeConstraint.size());
        setTextTransformation(sizeConstraint.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(sizeConstraint.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, Context context) {
        saveSizeConstraint(false);
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, Context context) {
        saveSizeConstraint(true);
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getTextTransformation());
        sb.append(" ").append(getComparisonOperator());
        sb.append(" ").append(getSize());

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

    protected abstract void saveSizeConstraint(boolean isDelete);

    private SizeConstraint toSizeConstraint() {
        return SizeConstraint.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .comparisonOperator(getComparisonOperator())
            .textTransformation(getTextTransformation())
            .size(getSize())
            .build();
    }

    protected UpdateSizeConstraintSetRequest.Builder toUpdateSizeConstraintSetRequest(boolean isDelete) {
        SizeConstraintSetResource parent = (SizeConstraintSetResource) parent();

        SizeConstraintSetUpdate sizeConstraintSetUpdate = SizeConstraintSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sizeConstraint(toSizeConstraint())
            .build();

        return UpdateSizeConstraintSetRequest.builder()
            .sizeConstraintSetId(parent.getId())
            .updates(sizeConstraintSetUpdate);
    }
}
