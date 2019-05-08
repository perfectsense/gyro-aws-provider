package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

//@ResourceName("regex-match-set")
public class RegexMatchSetResource extends gyro.aws.waf.common.RegexMatchSetResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRegexMatchSetId())) {
            return false;
        }

        GetRegexMatchSetResponse response = getGlobalClient().getRegexMatchSet(
                r -> r.regexMatchSetId(getRegexMatchSetId())
            );

        RegexMatchSet regexMatchSet = response.regexMatchSet();

        setName(regexMatchSet.name());

        getRegexMatchTuple().clear();

        for (RegexMatchTuple regexMatchTuple : regexMatchSet.regexMatchTuples()) {
            RegexMatchTupleResource regexMatchTupleResource = new RegexMatchTupleResource(regexMatchTuple);
            regexMatchTupleResource.parent(this);
            getRegexMatchTuple().add(regexMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateRegexMatchSetResponse response = client.createRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setRegexMatchSetId(response.regexMatchSet().regexMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(getRegexMatchSetId())
        );
    }
}
