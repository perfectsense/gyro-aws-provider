package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetUpdate;

import java.util.Set;

@ResourceName(parent = "size-constraint-set", value = "size-constraint")
public class SizeConstraintResource extends AwsResource {
    private String data;
    private String type;
    private String comparisonOperator;
    private String textTransformation;
    private Long size;

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
     * The comparison to be done on the filter. Valid values ```EQ```, ```NE```, ```LE```, ```LT```, ```GE```, ```GT```. (Required)
     */
    public String getComparisonOperator() {
        return comparisonOperator != null ? comparisonOperator.toUpperCase() : null;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
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

    /**
     * Size of the request in integers.
     */
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public SizeConstraintResource() {

    }

    public SizeConstraintResource(SizeConstraint sizeConstraint) {
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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveSizeConstraint(client, getSizeConstraint(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveSizeConstraint(client, getSizeConstraint(), true);
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

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private SizeConstraint getSizeConstraint() {
        return SizeConstraint.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .comparisonOperator(getComparisonOperator())
            .textTransformation(getTextTransformation())
            .size(getSize())
            .build();
    }

    private void saveSizeConstraint(WafClient client, SizeConstraint sizeConstraint, boolean isDelete) {
        SizeConstraintSetResource parent = (SizeConstraintSetResource) parent();

        SizeConstraintSetUpdate sizeConstraintSetUpdate = SizeConstraintSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .sizeConstraint(sizeConstraint)
            .build();

        client.updateSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(parent.getSizeConstraintSetId())
                .updates(sizeConstraintSetUpdate)
        );
    }
}
