package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

public class RegexMatchTupleResource extends gyro.aws.waf.common.RegexMatchTupleResource {
    @Override
    protected void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateRegexMatchSet(getUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
