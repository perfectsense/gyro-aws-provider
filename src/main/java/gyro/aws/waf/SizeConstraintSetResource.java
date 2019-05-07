package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a size constraint set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::size-constraint-set size-constraint-set-example
 *         name: "size-constraint-set-example"
 *
 *         size-constraint
 *             type: "METHOD"
 *             text-transformation: "NONE"
 *             comparison-operator: "EQ"
 *             size: 10
 *         end
 *     end
 */
@ResourceType("size-constraint-set")
public class SizeConstraintSetResource extends AwsResource {
    private String name;
    private String sizeConstraintSetId;
    private List<SizeConstraintResource> sizeConstraint;

    /**
     * The name of the size constraint condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getSizeConstraintSetId() {
        return sizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        this.sizeConstraintSetId = sizeConstraintSetId;
    }

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.SizeConstraintResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<SizeConstraintResource> getSizeConstraint() {
        if (sizeConstraint == null) {
            sizeConstraint = new ArrayList<>();
        }
        return sizeConstraint;
    }

    public void setSizeConstraint(List<SizeConstraintResource> sizeConstraint) {
        this.sizeConstraint = sizeConstraint;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getSizeConstraintSetId())) {
            return false;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetSizeConstraintSetResponse response = client.getSizeConstraintSet(
            r -> r.sizeConstraintSetId(getSizeConstraintSetId())
        );

        SizeConstraintSet sizeConstraintSet = response.sizeConstraintSet();
        setName(sizeConstraintSet.name());

        getSizeConstraint().clear();
        for (SizeConstraint sizeConstraint : sizeConstraintSet.sizeConstraints()) {
            SizeConstraintResource sizeConstraintResource = new SizeConstraintResource(sizeConstraint);
            sizeConstraintResource.parent(this);
            getSizeConstraint().add(sizeConstraintResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateSizeConstraintSetResponse response = client.createSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        SizeConstraintSet sizeConstraintSet = response.sizeConstraintSet();
        setSizeConstraintSetId(sizeConstraintSet.sizeConstraintSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(getSizeConstraintSetId())
        );
    }

    @Override
    public String toDisplayString() {

        StringBuilder sb = new StringBuilder();

        sb.append("size constraint set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getSizeConstraintSetId())) {
            sb.append(" - ").append(getSizeConstraintSetId());
        }

        return sb.toString();
    }
}
