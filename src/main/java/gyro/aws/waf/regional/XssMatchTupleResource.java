package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class XssMatchTupleResource extends gyro.aws.waf.common.XssMatchTupleResource {
    @Override
    protected void saveXssMatchTuple(XssMatchTuple xssMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateXssMatchSet(toUpdateXssMatchSetRequest(xssMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
