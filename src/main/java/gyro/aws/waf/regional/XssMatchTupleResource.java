package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class XssMatchTupleResource extends gyro.aws.waf.common.XssMatchTupleResource {
    public XssMatchTupleResource() {

    }

    public XssMatchTupleResource(XssMatchTuple xssMatchTuple) {
        setData(xssMatchTuple.fieldToMatch().data());
        setType(xssMatchTuple.fieldToMatch().typeAsString());
        setTextTransformation(xssMatchTuple.textTransformationAsString());
    }

    @Override
    protected void saveXssMatchTuple(XssMatchTuple xssMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateXssMatchSet(getUpdateXssMatchSetRequest(xssMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
