package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
        WafRegionalClient client = getRegionalClient();

        client.updateSizeConstraintSet(getUpdateSizeConstraintSetRequest(sizeConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
