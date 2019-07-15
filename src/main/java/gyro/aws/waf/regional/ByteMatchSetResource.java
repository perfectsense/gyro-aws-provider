package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a regional byte match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-byte-match-set-regional byte-match-set-example
 *     name: "byte-match-set-example"
 *
 *     byte-match-tuple
 *         field-to-match
 *             type: "METHOD"
 *         end
 *         text-transformation: "NONE"
 *         positional-constraint: "CONTAINS"
 *         target-string: "target-string"
 *     end
 * end
 */
@Type("waf-byte-match-set-regional")
public class ByteMatchSetResource extends gyro.aws.waf.common.ByteMatchSetResource {
    private Set<ByteMatchTupleResource> byteMatchTuple;

    /**
     * Set of byte match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.ByteMatchTupleResource
     */
    @Updatable
    public Set<ByteMatchTupleResource> getByteMatchTuple() {
        if (byteMatchTuple == null) {
            byteMatchTuple = new HashSet<>();
        }

        return byteMatchTuple;
    }

    public void setByteMatchTuple(Set<ByteMatchTupleResource> byteMatchTuple) {
        this.byteMatchTuple = byteMatchTuple;

        if (byteMatchTuple.size() > 10) {
            throw new GyroException("Byte Match Tuple limit exception. Max 10 per Byte Match Set.");
        }
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
    public void create(State state) {
        WafRegionalClient client = getRegionalClient();

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.byteMatchSet().byteMatchSetId());
    }

    @Override
    public void delete(State state) {
        WafRegionalClient client = getRegionalClient();

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getId())
        );
    }

    ByteMatchSet getByteMatchSet(WafRegionalClient client) {
        return client.getByteMatchSet(r -> r.byteMatchSetId(getId())).byteMatchSet();
    }
}
