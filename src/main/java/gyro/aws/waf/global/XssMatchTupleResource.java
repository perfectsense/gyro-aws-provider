package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.model.XssMatchSet;

import java.util.Set;
import java.util.stream.Collectors;

public class XssMatchTupleResource extends gyro.aws.waf.common.XssMatchTupleResource {

    @Override
    protected void saveXssMatchTuple(boolean isDelete) {
        WafClient client = getGlobalClient();

        try {
            saveTuple(client, isDelete);
        } catch (WafLimitsExceededException ex) {
            handleLimit(client);

            saveTuple(client, isDelete);
        }
    }

    private void saveTuple(WafClient client, boolean isDelete) {
        try {
            client.updateXssMatchSet(
                toUpdateXssMatchSetRequest(isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }

    private void handleLimit(WafClient client) {
        XssMatchSetResource parent = (XssMatchSetResource) parent();

        Set<String> pendingXssMatchTupleKeys = parent.getXssMatchTuple().stream().map(XssMatchTupleResource::primaryKey).collect(Collectors.toSet());

        XssMatchSet xssMatchSet = parent.getXssMatchSet(client);

        XssMatchSetResource xssMatchSetResource = new XssMatchSetResource();

        xssMatchSetResource.copyFrom(xssMatchSet);

        XssMatchTupleResource xssMatchTupleResource = xssMatchSetResource.getXssMatchTuple().stream().filter(o -> !pendingXssMatchTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (xssMatchTupleResource != null) {
            xssMatchTupleResource.saveTuple(client, true);
        }
    }
}
