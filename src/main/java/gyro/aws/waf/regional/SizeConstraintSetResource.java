package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

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
@Type("size-constraint-set-regional")
public class SizeConstraintSetResource extends gyro.aws.waf.common.SizeConstraintSetResource {
    private Set<SizeConstraintResource> sizeConstraint;

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.SizeConstraintResource
     */
    @Updatable
    public Set<SizeConstraintResource> getSizeConstraint() {
        if (sizeConstraint == null) {
            sizeConstraint = new HashSet<>();
        }
        return sizeConstraint;
    }

    public void setSizeConstraint(Set<SizeConstraintResource> sizeConstraint) {
        this.sizeConstraint = sizeConstraint;
    }

    @Override
    public void copyFrom(SizeConstraintSet sizeConstraintSet) {
        setId(sizeConstraintSet.sizeConstraintSetId());
        setName(sizeConstraintSet.name());

        getSizeConstraint().clear();
        for (SizeConstraint sizeConstraint : sizeConstraintSet.sizeConstraints()) {
            SizeConstraintResource sizeConstraintResource = newSubresource(SizeConstraintResource.class);
            sizeConstraintResource.copyFrom(sizeConstraint);
            getSizeConstraint().add(sizeConstraintResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetSizeConstraintSetResponse response = getRegionalClient().getSizeConstraintSet(
            r -> r.sizeConstraintSetId(getId())
        );


        this.copyFrom(response.sizeConstraintSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateSizeConstraintSetResponse response = client.createSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.sizeConstraintSet().sizeConstraintSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(getId())
        );
    }
}
