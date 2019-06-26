package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class RegexMatchTupleResource extends gyro.aws.waf.common.RegexMatchTupleResource {
    @Override
    protected void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateRegexMatchSet(toUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
