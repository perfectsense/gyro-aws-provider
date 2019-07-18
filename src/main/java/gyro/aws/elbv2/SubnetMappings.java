package gyro.aws.elbv2;

import gyro.aws.ec2.ElasticIpResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.SubnetMapping;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     subnet-mapping
 *         ip-address: $(aws::elastic-ip elastic-ip-example)
 *         subnet: $(aws::subnet subnet-example)
 *     end
 */
public class SubnetMappings extends Diffable {

    private ElasticIpResource ipAddress;
    private SubnetResource subnet;

    /**
     *  The elastic ip associated with the nlb. (Optional)
     */
    public ElasticIpResource getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(ElasticIpResource ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     *  The subnet associated with the nlb. (Required)
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getIpAddress() != null ? getIpAddress().getId() : null, getSubnet().getId());
    }

    public SubnetMapping toSubnetMappings() {
        return SubnetMapping.builder()
                .allocationId(getIpAddress() != null ? getIpAddress().getId() : null)
                .subnetId(getSubnet() != null ? getSubnet().getId() : null)
                .build();
    }
}
