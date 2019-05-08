package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName("size-constraint-set")
public class SizeConstraintSetResource extends gyro.aws.waf.common.SizeConstraintSetResource {
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
            sizeConstraintResource.parent(this);
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
