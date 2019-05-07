package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a byte match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::byte-match-set byte-match-set-example
 *         name: "byte-match-set-example"
 *
 *         byte-match-tuple
 *             type: "METHOD"
 *             text-transformation: "NONE"
 *             positional-constraint: "CONTAINS"
 *             target-string: "target-string"
 *         end
 *     end
 */
@ResourceName("byte-match-set")
public class ByteMatchSetResource extends AbstractWafResource {
    private String name;
    private String byteMatchSetId;
    private List<ByteMatchTupleResource> byteMatchTuple;

    /**
     * The name of the byte match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getByteMatchSetId() {
        return byteMatchSetId;
    }

    public void setByteMatchSetId(String byteMatchSetId) {
        this.byteMatchSetId = byteMatchSetId;
    }

    /**
     * List of byte match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.ByteMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
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

        GetByteMatchSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getByteMatchSet(
                r -> r.byteMatchSetId(getByteMatchSetId())
            );
        } else {
            response = getGlobalClient().getByteMatchSet(
                r -> r.byteMatchSetId(getByteMatchSetId())
            );
        }

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
        CreateByteMatchSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createByteMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createByteMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        ByteMatchSet byteMatchSet = response.byteMatchSet();

        setByteMatchSetId(byteMatchSet.byteMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteByteMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .byteMatchSetId(getByteMatchSetId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteByteMatchSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .byteMatchSetId(getByteMatchSetId())
            );
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("byte match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getByteMatchSetId())) {
            sb.append(" - ").append(getByteMatchSetId());
        }

        return sb.toString();
    }
}
