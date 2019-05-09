package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a size constraint set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::size-constraint-set-regional size-constraint-set-example
 *     name: "size-constraint-set-example"
 *
 *     size-constraint
 *         type: "METHOD"
 *         text-transformation: "NONE"
 *         comparison-operator: "EQ"
 *         size: 10
 *     end
 * end
 */
@ResourceType("size-constraint-set-regional")
public class SizeConstraintSetResource extends gyro.aws.waf.common.SizeConstraintSetResource {
    private List<SizeConstraintResource> sizeConstraint;

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.SizeConstraintResource
     */
    @ResourceUpdatable
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

        GetSizeConstraintSetResponse response = getRegionalClient().getSizeConstraintSet(
            r -> r.sizeConstraintSetId(getSizeConstraintSetId())
        );


        SizeConstraintSet sizeConstraintSet = response.sizeConstraintSet();
        setName(sizeConstraintSet.name());

        getSizeConstraint().clear();
        for (SizeConstraint sizeConstraint : sizeConstraintSet.sizeConstraints()) {
            SizeConstraintResource sizeConstraintResource = new SizeConstraintResource(sizeConstraint);
            getSizeConstraint().add(sizeConstraintResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateSizeConstraintSetResponse response = client.createSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setSizeConstraintSetId(response.sizeConstraintSet().sizeConstraintSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(getSizeConstraintSetId())
        );
    }
}
