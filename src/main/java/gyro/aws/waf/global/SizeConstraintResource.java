package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SizeConstraint;

//@ResourceName(parent = "size-constraint-set", value = "size-constraint")
public class SizeConstraintResource extends gyro.aws.waf.common.SizeConstraintResource {
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
    protected void saveSizeConstraint(SizeConstraint sizeConstraint, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateSizeConstraintSet(getUpdateSizeConstraintSetRequest(sizeConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
