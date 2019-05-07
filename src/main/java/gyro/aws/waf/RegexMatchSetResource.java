package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a regex match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::regex-match-set regex-match-set-example
 *         name: "regex-match-set-example"
 *         regex-match-tuple
 *             type: "METHOD"
 *             text-transformation: "NONE"
 *             regex-pattern-set-id: $(aws::regex-pattern-set regex-pattern-set-match-set-example | regex-pattern-set-id)
 *         end
 *     end
 */
@ResourceName("regex-match-set")
public class RegexMatchSetResource extends AbstractWafResource {
    private String name;
    private String regexMatchSetId;
    private List<RegexMatchTupleResource> regexMatchTuple;

    /**
     * The name of the regex match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getRegexMatchSetId() {
        return regexMatchSetId;
    }

    public void setRegexMatchSetId(String regexMatchSetId) {
        this.regexMatchSetId = regexMatchSetId;
    }

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.RegexMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
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

        GetRegexMatchSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getRegexMatchSet(
                r -> r.regexMatchSetId(getRegexMatchSetId())
            );
        } else {
            response = getGlobalClient().getRegexMatchSet(
                r -> r.regexMatchSetId(getRegexMatchSetId())
            );
        }

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
        CreateRegexMatchSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createRegexMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createRegexMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        RegexMatchSet regexMatchSet = response.regexMatchSet();

        setRegexMatchSetId(regexMatchSet.regexMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteRegexMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .regexMatchSetId(getRegexMatchSetId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteRegexMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .regexMatchSetId(getRegexMatchSetId())
            );
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("regex match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getRegexMatchSetId())) {
            sb.append(" - ").append(getRegexMatchSetId());
        }

        return sb.toString();
    }
}
