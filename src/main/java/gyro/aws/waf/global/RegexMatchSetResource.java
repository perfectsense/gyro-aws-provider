package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global regex match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::regex-pattern-set regex-pattern-set-match-set-example
 *     name: "regex-pattern-set-match-set-example"
 *
 *     patterns: [
 *         "pattern1",
 *         "pattern2"
 *     ]
 * end
 *
 * aws::regex-match-set regex-match-set-example
 *     name: "regex-match-set-example"
 *     regex-match-tuple
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *         regex-pattern-set-id: $(aws::regex-pattern-set regex-pattern-set-match-set-example | regex-pattern-set-id)
 *     end
 * end
 */
@ResourceType("regex-match-set")
public class RegexMatchSetResource extends gyro.aws.waf.common.RegexMatchSetResource {
    private List<RegexMatchTupleResource> regexMatchTuple;

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.RegexMatchTupleResource
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

        GetRegexMatchSetResponse response = getGlobalClient().getRegexMatchSet(
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
