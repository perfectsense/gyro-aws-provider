package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;

import java.nio.charset.StandardCharsets;

public class ByteMatchTupleResource extends gyro.aws.waf.common.ByteMatchTupleResource {
    @Override
    protected void saveByteMatchTuple(ByteMatchTuple byteMatchTuple, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateByteMatchSet(
            getByteMatchSetUpdateRequest(byteMatchTuple, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
        );
    }
}
