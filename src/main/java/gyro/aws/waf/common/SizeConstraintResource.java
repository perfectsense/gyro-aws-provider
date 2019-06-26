package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateSizeConstraintSetRequest;

import java.util.Set;

public abstract class SizeConstraintResource extends AbstractWafResource implements Copyable<SizeConstraint> {
    private String data;
    private String type;
    private String comparisonOperator;
    private String textTransformation;
    private Long size;

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
     * Part of the request to filter on. Valid values are ``URI`` or ``QUERY_STRING`` or ``HEADER`` or ``METHOD`` or ``BODY`` or ``SINGLE_QUERY_ARG`` or ``ALL_QUERY_ARGS``. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
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
        setData(sizeConstraint.fieldToMatch().data());
        setType(sizeConstraint.fieldToMatch().typeAsString());
        setSize(sizeConstraint.size());
        setTextTransformation(sizeConstraint.textTransformationAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveSizeConstraint(toSizeConstraint(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveSizeConstraint(toSizeConstraint(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("size constraint");

        if (!ObjectUtils.isBlank(getData())) {
            sb.append(" - ").append(getData());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getComparisonOperator())) {
            sb.append(" - ").append(getComparisonOperator());
        }

        if (!ObjectUtils.isBlank(getSize())) {
            sb.append(" - ").append(getSize());
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s %s %s", getData(), getType(), getComparisonOperator(), getTextTransformation(), getSize());
    }

    protected abstract void saveSizeConstraint(SizeConstraint sizeConstraint, boolean isDelete);

    private SizeConstraint toSizeConstraint() {
        return SizeConstraint.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .comparisonOperator(getComparisonOperator())
            .textTransformation(getTextTransformation())
            .size(getSize())
            .build();
    }

    protected UpdateSizeConstraintSetRequest.Builder toUpdateSizeConstraintSetRequest(SizeConstraint sizeConstraint, boolean isDelete) {
        SizeConstraintSetResource parent = (SizeConstraintSetResource) parent();

        SizeConstraintSetUpdate sizeConstraintSetUpdate = SizeConstraintSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sizeConstraint(sizeConstraint)
            .build();

        return UpdateSizeConstraintSetRequest.builder()
            .sizeConstraintSetId(parent.getId())
            .updates(sizeConstraintSetUpdate);
    }
}
