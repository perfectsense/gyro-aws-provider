package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

import java.util.List;
import java.util.Set;

@ResourceName("size-constraint-set")
public class SizeConstraintSetResource extends AwsResource {
    private String name;
    private String sizeConstraintSetId;
    private List<SizeConstraintResource> sizeConstraint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSizeConstraintSetId() {
        return sizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        this.sizeConstraintSetId = sizeConstraintSetId;
    }

    public List<SizeConstraintResource> getSizeConstraint() {
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
