package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;


//@ResourceName("byte-match-set")
public class ByteMatchSetResource extends gyro.aws.waf.common.ByteMatchSetResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getByteMatchSetId())) {
            return false;
        }

        GetByteMatchSetResponse response = getRegionalClient().getByteMatchSet(r -> r.byteMatchSetId(getByteMatchSetId()));

        ByteMatchSet byteMatchSet = response.byteMatchSet();

        setName(byteMatchSet.name());

        getByteMatchTuple().clear();

        for (ByteMatchTuple byteMatchTuple : byteMatchSet.byteMatchTuples()) {
            ByteMatchTupleResource byteMatchTupleResource = new ByteMatchTupleResource(byteMatchTuple);
            byteMatchTupleResource.parent(this);
            getByteMatchTuple().add(byteMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setByteMatchSetId(response.byteMatchSet().byteMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getByteMatchSetId())
        );
    }
}
