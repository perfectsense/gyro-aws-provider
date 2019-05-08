package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

//@ResourceName("xss-match-set")
public class XssMatchSetResource extends gyro.aws.waf.common.XssMatchSetResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getXssMatchSetId())) {
            return false;
        }

        GetXssMatchSetResponse response = getGlobalClient().getXssMatchSet(
                r -> r.xssMatchSetId(getXssMatchSetId())
            );

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
        WafClient client = getGlobalClient();

        CreateXssMatchSetResponse response = client.createXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setXssMatchSetId(response.xssMatchSet().xssMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .xssMatchSetId(getXssMatchSetId())
        );
    }
}
