package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
        WafRegionalClient client = getRegionalClient();

        client.updateRegexMatchSet(getUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
