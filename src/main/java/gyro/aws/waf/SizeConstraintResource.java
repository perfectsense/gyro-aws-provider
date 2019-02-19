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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public String getTextTransformation() {
        return textTransformation;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

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
