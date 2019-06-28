package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class SizeConstraintResource extends gyro.aws.waf.common.SizeConstraintResource {
    @Override
    protected void saveSizeConstraint(SizeConstraint sizeConstraint, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateSizeConstraintSet(getUpdateSizeConstraintSetRequest(sizeConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
