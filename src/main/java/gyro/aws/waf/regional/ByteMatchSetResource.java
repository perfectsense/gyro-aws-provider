package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a regional byte match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::byte-match-set-regional byte-match-set-example
 *     name: "byte-match-set-example"
 *
 *     byte-match-tuple
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *         positional-constraint: "CONTAINS"
 *         target-string: "target-string"
 *     end
 * end
 */
@Type("byte-match-set-regional")
public class ByteMatchSetResource extends gyro.aws.waf.common.ByteMatchSetResource {
    private List<ByteMatchTupleResource> byteMatchTuple;

    /**
     * List of byte match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.ByteMatchTupleResource
     */
    @Updatable
    public List<ByteMatchTupleResource> getByteMatchTuple() {
        if (byteMatchTuple == null) {
            byteMatchTuple = new ArrayList<>();
        }

        return byteMatchTuple;
    }

    public void setByteMatchTuple(List<ByteMatchTupleResource> byteMatchTuple) {
        this.byteMatchTuple = byteMatchTuple;
    }

    @Override
    public void copyFrom(ByteMatchSet byteMatchSet) {
        setId(byteMatchSet.byteMatchSetId());
        setName(byteMatchSet.name());

        getByteMatchTuple().clear();

        for (ByteMatchTuple byteMatchTuple : byteMatchSet.byteMatchTuples()) {
            ByteMatchTupleResource byteMatchTupleResource = newSubresource(ByteMatchTupleResource.class);
            byteMatchTupleResource.copyFrom(byteMatchTuple);
            getByteMatchTuple().add(byteMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetByteMatchSetResponse response = getRegionalClient().getByteMatchSet(r -> r.byteMatchSetId(getId()));

        this.copyFrom(response.byteMatchSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.byteMatchSet().byteMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getId())
        );
    }
}
