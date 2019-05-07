package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a xss match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::xss-match-set xss-match-set-example
 *         name: "xss-match-set-example"
 *
 *         xss-match-tuple
 *             type: "METHOD"
 *             text-transformation: "NONE"
 *         end
 *     end
 */
@ResourceName("xss-match-set")
public class XssMatchSetResource extends AbstractWafResource {
    private String name;
    private String xssMatchSetId;
    private List<XssMatchTupleResource> xssMatchTuple;

    /**
     * The name of the xss match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getXssMatchSetId() {
        return xssMatchSetId;
    }

    public void setXssMatchSetId(String xssMatchSetId) {
        this.xssMatchSetId = xssMatchSetId;
    }

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.XssMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<XssMatchTupleResource> getXssMatchTuple() {
        if (xssMatchTuple == null) {
            xssMatchTuple = new ArrayList<>();
        }

        return xssMatchTuple;
    }

    public void setXssMatchTuple(List<XssMatchTupleResource> xssMatchTuple) {
        this.xssMatchTuple = xssMatchTuple;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getXssMatchSetId())) {
            return false;
        }

        GetXssMatchSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getXssMatchSet(
                r -> r.xssMatchSetId(getXssMatchSetId())
            );
        } else {
            response = getGlobalClient().getXssMatchSet(
                r -> r.xssMatchSetId(getXssMatchSetId())
            );
        }

        XssMatchSet xssMatchSet = response.xssMatchSet();
        setName(xssMatchSet.name());

        getXssMatchTuple().clear();
        for (XssMatchTuple xssMatchTuple : xssMatchSet.xssMatchTuples()) {
            XssMatchTupleResource xssMatchTupleResource = new XssMatchTupleResource(xssMatchTuple);
            xssMatchTupleResource.parent(this);
            getXssMatchTuple().add(xssMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        CreateXssMatchSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createXssMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createXssMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        XssMatchSet xssMatchSet = response.xssMatchSet();
        setXssMatchSetId(xssMatchSet.xssMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteXssMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .xssMatchSetId(getXssMatchSetId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteXssMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .xssMatchSetId(getXssMatchSetId())
            );
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("xss match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getXssMatchSetId())) {
            sb.append(" - ").append(getXssMatchSetId());
        }

        return sb.toString();
    }
}
