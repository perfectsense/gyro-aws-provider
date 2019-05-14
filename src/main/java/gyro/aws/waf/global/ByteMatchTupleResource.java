package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;

import java.nio.charset.StandardCharsets;

public class ByteMatchTupleResource extends gyro.aws.waf.common.ByteMatchTupleResource {
    public ByteMatchTupleResource() {

    }

    public ByteMatchTupleResource(ByteMatchTuple byteMatchTuple) {
        setType(byteMatchTuple.fieldToMatch().typeAsString());
        setData(byteMatchTuple.fieldToMatch().data());
        setPositionalConstraint(byteMatchTuple.positionalConstraintAsString());
        setTargetString(byteMatchTuple.targetString().asString(StandardCharsets.UTF_8));
        setTextTransformation(byteMatchTuple.textTransformationAsString());
    }

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
