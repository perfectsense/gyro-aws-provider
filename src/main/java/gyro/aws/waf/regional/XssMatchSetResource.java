package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a xss match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::xss-match-set-regional xss-match-set-example
 *     name: "xss-match-set-example"
 *
 *     xss-match-tuple
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *     end
 * end
 */
@Type("xss-match-set-regional")
public class XssMatchSetResource extends gyro.aws.waf.common.XssMatchSetResource {
    private Set<XssMatchTupleResource> xssMatchTuple;

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.XssMatchTupleResource
     */
    @Updatable
    public Set<XssMatchTupleResource> getXssMatchTuple() {
        if (xssMatchTuple == null) {
            xssMatchTuple = new HashSet<>();
        }

        return xssMatchTuple;
    }

    public void setXssMatchTuple(Set<XssMatchTupleResource> xssMatchTuple) {
        this.xssMatchTuple = xssMatchTuple;
    }

    @Override
    public void copyFrom(XssMatchSet xssMatchSet) {
        setId(xssMatchSet.xssMatchSetId());
        setName(xssMatchSet.name());

        getXssMatchTuple().clear();
        for (XssMatchTuple xssMatchTuple : xssMatchSet.xssMatchTuples()) {
            XssMatchTupleResource xssMatchTupleResource = newSubresource(XssMatchTupleResource.class);
            xssMatchTupleResource.copyFrom(xssMatchTuple);
            getXssMatchTuple().add(xssMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetXssMatchSetResponse response = getRegionalClient().getXssMatchSet(
            r -> r.xssMatchSetId(getId())
        );

        this.copyFrom(response.xssMatchSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateXssMatchSetResponse response = client.createXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.xssMatchSet().xssMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .xssMatchSetId(getId())
        );
    }
}
