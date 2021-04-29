package gyro.aws.ec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacementRequest;
import software.amazon.awssdk.services.ec2.model.Tenancy;

public class LaunchTemplatePlacement extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacement> {

    private String affinity;
    private String availabilityZone;
    private String groupName;
    private String hostId;
    private String hostResourceGroupArn;
    private Integer partitionNumber;
    private Tenancy tenancy;

    /**
     * The affinity setting for an instance on a Dedicated Host.
     */
    @Updatable
    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    /**
     * The availability zone for the instance.
     */
    @Updatable
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The name of the placement group for the instance.
     */
    @Updatable
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The ID of the dedicated host for the instance.
     */
    @Updatable
    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    /**
     * The ARN of the host resource group in which to launch the instances.
     */
    @Updatable
    public String getHostResourceGroupArn() {
        return hostResourceGroupArn;
    }

    public void setHostResourceGroupArn(String hostResourceGroupArn) {
        this.hostResourceGroupArn = hostResourceGroupArn;
    }

    /**
     * The number of the partition the instance should launch in.
     */
    @Updatable
    public Integer getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(Integer partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    /**
     * The tenancy of the instance.
     */
    @Updatable
    @ValidStrings({ "DEFAULT", "DEDICATED", "HOST" })
    public Tenancy getTenancy() {
        return tenancy;
    }

    public void setTenancy(Tenancy tenancy) {
        this.tenancy = tenancy;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplatePlacement model) {
        setAffinity(model.affinity());
        setAvailabilityZone(model.availabilityZone());
        setGroupName(model.groupName());
        setHostId(model.hostId());
        setHostResourceGroupArn(model.hostResourceGroupArn());
        setPartitionNumber(model.partitionNumber());
        setTenancy(model.tenancy());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("host-resource-group-arn") && !(getTenancy() == null || getTenancy().equals(
            Tenancy.HOST))) {
            errors.add(new ValidationError(this, null,
                "if 'host-resource-group-arn' is set, 'tenancy' can only be se to 'HOST'."));
        }

        return errors;
    }

    LaunchTemplatePlacementRequest toLaunchTemplatePlacementRequest() {
        return LaunchTemplatePlacementRequest.builder()
            .affinity(getAffinity())
            .availabilityZone(getAvailabilityZone())
            .groupName(getGroupName())
            .hostId(getHostId())
            .hostResourceGroupArn(getHostResourceGroupArn())
            .partitionNumber(getPartitionNumber())
            .tenancy(getTenancy())
            .build();
    }
}
