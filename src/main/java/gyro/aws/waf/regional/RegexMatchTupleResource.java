package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class RegexMatchTupleResource extends gyro.aws.waf.common.RegexMatchTupleResource {
    @Override
    protected void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        // Handle if replacing regex match tuple.
        if (!isDelete) {
            RegexMatchSetResource parent = (RegexMatchSetResource) parent();

            RegexMatchSet regexMatchSet = parent.getRegexMatchSet(client);

            if (!regexMatchSet.regexMatchTuples().isEmpty()) {
                client.updateRegexMatchSet(toUpdateRegexMatchSetRequest(regexMatchSet.regexMatchTuples().get(0), true)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
                );
            }
        }

        try {
            client.updateRegexMatchSet(toUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }
}
