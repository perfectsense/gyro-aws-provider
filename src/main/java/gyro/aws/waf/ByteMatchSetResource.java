package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.CreateByteMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetByteMatchSetResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("byte-match-set")
public class ByteMatchSetResource extends AwsResource {
    private String name;
    private String byteMatchSetId;
    private List<ByteMatchTupleResource> byteMatchTuple;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getByteMatchSetId() {
        return byteMatchSetId;
    }

    public void setByteMatchSetId(String byteMatchSetId) {
        this.byteMatchSetId = byteMatchSetId;
    }

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

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetByteMatchSetResponse response = client.getByteMatchSet(
            r -> r.byteMatchSetId(getByteMatchSetId())
        );

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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateByteMatchSetResponse response = client.createByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        ByteMatchSet byteMatchSet = response.byteMatchSet();

        setByteMatchSetId(byteMatchSet.byteMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(getByteMatchSetId())
        );
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
