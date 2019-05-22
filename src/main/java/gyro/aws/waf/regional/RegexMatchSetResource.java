package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a regional regex match set.
 *
 * Example
 * -------
 *
 * aws::regex-pattern-set-regional regex-pattern-set-match-set-example
 *     name: "regex-pattern-set-match-set-example"
 *
 *     patterns: [
 *         "pattern1",
 *         "pattern2"
 *     ]
 * end
 *
 * aws::regex-match-set-regional regex-match-set-example
 *     name: "regex-match-set-example"
 *     regex-match-tuple
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *         regex-pattern-set-id: $(aws::regex-pattern-set-regional regex-pattern-set-match-set-example | regex-pattern-set-id)
 *     end
 * end
 */
@Type("regex-match-set-regional")
public class RegexMatchSetResource extends gyro.aws.waf.common.RegexMatchSetResource {
    private List<RegexMatchTupleResource> regexMatchTuple;

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.RegexMatchTupleResource
     */
    @Updatable
    public List<RegexMatchTupleResource> getRegexMatchTuple() {
        if (regexMatchTuple == null) {
            regexMatchTuple = new ArrayList<>();
        }

        return regexMatchTuple;
    }

    public void setRegexMatchTuple(List<RegexMatchTupleResource> regexMatchTuple) {
        this.regexMatchTuple = regexMatchTuple;
    }
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRegexMatchSetId())) {
            return false;
        }

        GetRegexMatchSetResponse response = getRegionalClient().getRegexMatchSet(
            r -> r.regexMatchSetId(getRegexMatchSetId())
        );

        RegexMatchSet regexMatchSet = response.regexMatchSet();

        setName(regexMatchSet.name());

        getRegexMatchTuple().clear();

        for (RegexMatchTuple regexMatchTuple : regexMatchSet.regexMatchTuples()) {
            RegexMatchTupleResource regexMatchTupleResource = new RegexMatchTupleResource(regexMatchTuple);
            getRegexMatchTuple().add(regexMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateRegexMatchSetResponse response = client.createRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setRegexMatchSetId(response.regexMatchSet().regexMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(getRegexMatchSetId())
        );
    }
}
