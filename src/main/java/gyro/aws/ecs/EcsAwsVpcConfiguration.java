package gyro.aws.ecs;

import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;

public class EcsAwsVpcConfiguration extends Diffable implements Copyable<AwsVpcConfiguration> {

    private AssignPublicIp assignPublicIp;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;

    /**
     * Option to select whether the task's elastic network interface receives a public IP address.
     */
    @Updatable
    public AssignPublicIp getAssignPublicIp() {
        return assignPublicIp;
    }

    public void setAssignPublicIp(AssignPublicIp assignPublicIp) {
        this.assignPublicIp = assignPublicIp;
    }

    /**
     * The security groups associated with the task or service. (Required)
     */
    @Required
    @Updatable
    public List<SecurityGroupResource> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The subnets associated with the task or service. (Required)
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AwsVpcConfiguration model) {
        setSubnets((model.subnets().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList())));
        setSecurityGroups(model.securityGroups()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
        setAssignPublicIp(model.assignPublicIp());
    }

    public AwsVpcConfiguration toAwsVpcConfiguration() {
        return AwsVpcConfiguration.builder()
            .assignPublicIp(getAssignPublicIp())
            .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(
                Collectors.toList()))
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(
                Collectors.toList()))
            .build();
    }
}
