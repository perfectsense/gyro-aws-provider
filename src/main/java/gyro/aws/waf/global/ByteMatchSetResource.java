package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a global byte match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-byte-match-set byte-match-set-example
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
@Type("waf-byte-match-set")
public class ByteMatchSetResource extends gyro.aws.waf.common.ByteMatchSetResource {
    private Set<ByteMatchTupleResource> byteMatchTuple;

    /**
     * List of byte match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.ByteMatchTupleResource
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

        GetByteMatchSetResponse response = getGlobalClient().getByteMatchSet(r -> r.byteMatchSetId(getId()));

        copyFrom(response.byteMatchSet());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.byteMatchSet().byteMatchSetId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getId())
        );
    }

    ByteMatchSet getByteMatchSet(WafClient client) {
        return client.getByteMatchSet(r -> r.byteMatchSetId(getId())).byteMatchSet();
    }
}
