package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class ByteMatchTupleResource extends gyro.aws.waf.common.ByteMatchTupleResource {
    @Override
    protected void saveByteMatchTuple(ByteMatchTuple byteMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateByteMatchSet(
            getByteMatchSetUpdateRequest(byteMatchTuple, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
        );
    }
}
