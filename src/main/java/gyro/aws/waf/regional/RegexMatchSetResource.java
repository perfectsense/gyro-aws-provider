package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

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
 * aws::waf-regex-match-set-regional regex-match-set-example
 *     name: "regex-match-set-example"
 *     regex-match-tuple
 *         field-to-match
 *             type: "METHOD"
 *         end
 *         text-transformation: "NONE"
 *         regex-pattern-set: $(aws::regex-pattern-set-regional regex-pattern-set-match-set-example)
 *     end
 * end
 */
@Type("waf-regex-match-set-regional")
public class RegexMatchSetResource extends gyro.aws.waf.common.RegexMatchSetResource {
    private Set<RegexMatchTupleResource> regexMatchTuple;

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.RegexMatchTupleResource
     */
    @Updatable
    public Set<RegexMatchTupleResource> getRegexMatchTuple() {
        if (regexMatchTuple == null) {
            regexMatchTuple = new HashSet<>();
        }

        return regexMatchTuple;
    }

    public void setRegexMatchTuple(Set<RegexMatchTupleResource> regexMatchTuple) {
        this.regexMatchTuple = regexMatchTuple;
    }

    @Override
    public void copyFrom(RegexMatchSet regexMatchSet) {
        setId(regexMatchSet.regexMatchSetId());
        setName(regexMatchSet.name());

        getRegexMatchTuple().clear();

        for (RegexMatchTuple regexMatchTuple : regexMatchSet.regexMatchTuples()) {
            RegexMatchTupleResource regexMatchTupleResource = newSubresource(RegexMatchTupleResource.class);
            regexMatchTupleResource.copyFrom(regexMatchTuple);
            getRegexMatchTuple().add(regexMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetRegexMatchSetResponse response = getRegionalClient().getRegexMatchSet(
            r -> r.regexMatchSetId(getId())
        );

        this.copyFrom(response.regexMatchSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateRegexMatchSetResponse response = client.createRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.regexMatchSet().regexMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(getId())
        );
    }
}
