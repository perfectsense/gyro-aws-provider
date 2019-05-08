package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;

@ResourceType("xss-match-set-regional")
public class XssMatchSetResource extends gyro.aws.waf.common.XssMatchSetResource {
    private List<XssMatchTupleResource> xssMatchTuple;

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.XssMatchTupleResource
     */
    @ResourceUpdatable
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

        GetXssMatchSetResponse response = getRegionalClient().getXssMatchSet(
            r -> r.xssMatchSetId(getXssMatchSetId())
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
        WafRegionalClient client = getRegionalClient();

        CreateXssMatchSetResponse response = client.createXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setXssMatchSetId(response.xssMatchSet().xssMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .xssMatchSetId(getXssMatchSetId())
        );
    }
}
