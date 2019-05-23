package gyro.aws.elbv2;

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
 *         allocation-id: $(aws::elastic-ip elastic-ip-example | allocation-id)
 *         ip-address: $(aws::elastic-ip elastic-ip-example | public-ip)
 *         subnet: $(aws::subnet subnet-example)
 *     end
 */

public class SubnetMappings extends Diffable {

    private String allocationId;
    private String ipAddress;
    private SubnetResource subnet;

    /**
     *  The allocation id associated with the elastic ip (Optional)
     */
    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    /**
     *  The public ip associated with the elastic ip (Optional)
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     *  The subnet associated with the nlb (Optional)
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = this.subnet;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getAllocationId(), getIpAddress(), getSubnet());
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getAllocationId() != null) {
            sb.append("subnet mapping - " + getAllocationId());
        } else {
            sb.append("subnet mapping ");
        }

        return sb.toString();
    }

    public SubnetMapping toSubnetMappings() {
        return SubnetMapping.builder()
                .allocationId(getAllocationId())
                .subnetId(getSubnet().getSubnetId())
                .build();
    }
}
