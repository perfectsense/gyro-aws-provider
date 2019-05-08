package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

import java.util.ArrayList;
import java.util.List;

@ResourceType("size-constraint-set")
public class SizeConstraintSetResource extends gyro.aws.waf.common.SizeConstraintSetResource {
    private List<SizeConstraintResource> sizeConstraint;

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.SizeConstraintResource
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

        GetSizeConstraintSetResponse response = getGlobalClient().getSizeConstraintSet(
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
        WafClient client = getGlobalClient();

        CreateSizeConstraintSetResponse response = client.createSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setSizeConstraintSetId(response.sizeConstraintSet().sizeConstraintSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(getSizeConstraintSetId())
        );
    }
}
