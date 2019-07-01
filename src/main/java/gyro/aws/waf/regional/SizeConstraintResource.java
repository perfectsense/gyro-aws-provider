package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;
import java.util.stream.Collectors;

public class SizeConstraintResource extends gyro.aws.waf.common.SizeConstraintResource {
    @Override
    protected void saveSizeConstraint(boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        try {
            saveTuple(client, isDelete);
        } catch (WafLimitsExceededException ex) {
            handleLimit(client);

            saveTuple(client, isDelete);
        }
    }

    private void saveTuple(WafRegionalClient client, boolean isDelete) {
        try {
            client.updateSizeConstraintSet(
                toUpdateSizeConstraintSetRequest(isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }

    private void handleLimit(WafRegionalClient client) {
        SizeConstraintSetResource parent = (SizeConstraintSetResource) parent();

        Set<String> pendingSizeConstraintTupleKeys = parent.getSizeConstraint().stream().map(SizeConstraintResource::primaryKey).collect(Collectors.toSet());

        SizeConstraintSet sizeConstraintSet = parent.getSizeConstraintSet(client);

        SizeConstraintSetResource sizeConstraintSetResource = new SizeConstraintSetResource();

        sizeConstraintSetResource.copyFrom(sizeConstraintSet);

        SizeConstraintResource sizeConstraintResource = sizeConstraintSetResource.getSizeConstraint().stream().filter(o -> !pendingSizeConstraintTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (sizeConstraintResource != null) {
            sizeConstraintResource.saveTuple(client, true);
        }
    }
}
