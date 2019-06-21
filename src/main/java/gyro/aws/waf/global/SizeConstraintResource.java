package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SizeConstraint;

public class SizeConstraintResource extends gyro.aws.waf.common.SizeConstraintResource {
    @Override
    protected void saveSizeConstraint(SizeConstraint sizeConstraint, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateSizeConstraintSet(getUpdateSizeConstraintSetRequest(sizeConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
