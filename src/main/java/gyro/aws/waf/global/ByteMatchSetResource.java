package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global byte match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::byte-match-set byte-match-set-example
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
@Type("byte-match-set")
public class ByteMatchSetResource extends gyro.aws.waf.common.ByteMatchSetResource {
    private List<ByteMatchTupleResource> byteMatchTuple;

    /**
     * List of byte match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.ByteMatchTupleResource
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
    public boolean refresh() {
        if (ObjectUtils.isBlank(getByteMatchSetId())) {
            return false;
        }

        GetByteMatchSetResponse response = getGlobalClient().getByteMatchSet(r -> r.byteMatchSetId(getByteMatchSetId()));

        ByteMatchSet byteMatchSet = response.byteMatchSet();

        setName(byteMatchSet.name());

        getByteMatchTuple().clear();

        for (ByteMatchTuple byteMatchTuple : byteMatchSet.byteMatchTuples()) {
            ByteMatchTupleResource byteMatchTupleResource = new ByteMatchTupleResource(byteMatchTuple);
            getByteMatchTuple().add(byteMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setByteMatchSetId(response.byteMatchSet().byteMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getByteMatchSetId())
        );
    }
}
