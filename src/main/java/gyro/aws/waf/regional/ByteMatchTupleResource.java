package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;
import java.util.stream.Collectors;

public class ByteMatchTupleResource extends gyro.aws.waf.common.ByteMatchTupleResource {
    @Override
    protected void saveByteMatchTuple(boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        try {
            saveTuple(client, isDelete);
        } catch (WafLimitsExceededException ex) {
            handleLimit(client);

            saveTuple(client, isDelete);
        }
    }

    private void saveTuple(WafRegionalClient client, boolean isDelete) {
        try {
            client.updateByteMatchSet(
                toByteMatchSetUpdateRequest(isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }

    private void handleLimit(WafRegionalClient client) {
        ByteMatchSetResource parent = (ByteMatchSetResource) parent();

        Set<String> pendingByteMatchTupleKeys = parent.getByteMatchTuple().stream().map(ByteMatchTupleResource::primaryKey).collect(Collectors.toSet());

        ByteMatchSet byteMatchSet = parent.getByteMatchSet(client);

        ByteMatchSetResource byteMatchSetResource = new ByteMatchSetResource();

        byteMatchSetResource.copyFrom(byteMatchSet);

        ByteMatchTupleResource byteMatchTupleResource = byteMatchSetResource.getByteMatchTuple().stream().filter(o -> !pendingByteMatchTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (byteMatchTupleResource != null) {
            byteMatchTupleResource.saveTuple(client, true);
        }
    }
}
