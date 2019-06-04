package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.route53.model.VPC;

import java.util.Set;

public class Route53VpcResource extends AwsResource implements Copyable<VPC> {
    private VpcResource vpc;
    private String vpcRegion;

    /**
     * The vpc.
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The region the vpc resides in.
     */
    public String getVpcRegion() {
        return vpcRegion;
    }

    public void setVpcRegion(String vpcRegion) {
        this.vpcRegion = vpcRegion;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        HostedZoneResource parent = getParent();

        try {
            parent.saveVpc(getVpc().getVpcId(), getVpcRegion(), true);
        } catch (Route53Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equalsIgnoreCase("ConflictingDomainExists")) {
                throw ex;
            }
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete() {
        HostedZoneResource parent = getParent();

        try {
            parent.saveVpc(getVpc().getVpcId(), getVpcRegion(), false);
        } catch (Route53Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equalsIgnoreCase("LastVpcAssociation")) {
                throw ex;
            }
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("route53 vpc ").append(" [ ");

        if (getVpc() != null) {
            sb.append(getVpc().getVpcId()).append(" - ");
        }

        if (!ObjectUtils.isBlank(getVpcRegion())) {
            sb.append(getVpcRegion()).append(" ]");
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getVpc() != null ? getVpc().getVpcId() : null, getVpcRegion());
    }

    private HostedZoneResource getParent() {
        HostedZoneResource parent = (HostedZoneResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent hosted zone resource not found.");
        }
        return parent;
    }

    @Override
    public void copyFrom(VPC vpc) {
        this.vpc = findById(VpcResource.class, vpc.vpcId());
        this.vpcRegion = vpc.vpcRegionAsString();
    }
}
