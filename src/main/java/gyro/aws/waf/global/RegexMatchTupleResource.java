package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

//@ResourceName(parent = "regex-match-set", value = "regex-match-tuple")
public class RegexMatchTupleResource extends gyro.aws.waf.common.RegexMatchTupleResource {
    public RegexMatchTupleResource() {

    }

    public RegexMatchTupleResource(RegexMatchTuple regexMatchTuple) {
        setType(regexMatchTuple.fieldToMatch().typeAsString());
        setData(regexMatchTuple.fieldToMatch().data());
        setRegexPatternSetId(regexMatchTuple.regexPatternSetId());
        setTextTransformation(regexMatchTuple.textTransformationAsString());
    }
    @Override
    protected void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateRegexMatchSet(getUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
