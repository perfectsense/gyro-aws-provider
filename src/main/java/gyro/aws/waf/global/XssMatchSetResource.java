package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global xss match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::xss-match-set xss-match-set-example
 *     name: "xss-match-set-example"
 *
 *     xss-match-tuple
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *     end
 * end
 */
@Type("xss-match-set")
public class XssMatchSetResource extends gyro.aws.waf.common.XssMatchSetResource {
    private List<XssMatchTupleResource> xssMatchTuple;

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.XssMatchTupleResource
     */
    @Updatable
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
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetXssMatchSetResponse response = getGlobalClient().getXssMatchSet(
                r -> r.xssMatchSetId(getId())
            );

        XssMatchSet xssMatchSet = response.xssMatchSet();
        setName(xssMatchSet.name());

        getXssMatchTuple().clear();
        for (XssMatchTuple xssMatchTuple : xssMatchSet.xssMatchTuples()) {
            XssMatchTupleResource xssMatchTupleResource = new XssMatchTupleResource(xssMatchTuple);
            getXssMatchTuple().add(xssMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateXssMatchSetResponse response = client.createXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.xssMatchSet().xssMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .xssMatchSetId(getId())
        );
    }
}
