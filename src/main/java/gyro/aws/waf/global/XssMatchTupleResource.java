package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

public class XssMatchTupleResource extends gyro.aws.waf.common.XssMatchTupleResource {
    @Override
    protected void saveXssMatchTuple(XssMatchTuple xssMatchTuple, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateXssMatchSet(getUpdateXssMatchSetRequest(xssMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
