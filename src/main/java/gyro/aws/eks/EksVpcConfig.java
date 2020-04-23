package gyro.aws.eks;

import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.VpcConfigRequest;
import software.amazon.awssdk.services.eks.model.VpcConfigResponse;

public class EksVpcConfig extends Diffable implements Copyable<VpcConfigResponse> {

    private Boolean enableEndpointPrivateAccess;
    private Boolean enableEndpointPublicAccess;
    private List<String> publicAccessCidrs;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;

    public Boolean getEnableEndpointPrivateAccess() {
        return enableEndpointPrivateAccess;
    }

    public void setEnableEndpointPrivateAccess(Boolean enableEndpointPrivateAccess) {
        this.enableEndpointPrivateAccess = enableEndpointPrivateAccess;
    }

    public Boolean getEnableEndpointPublicAccess() {
        return enableEndpointPublicAccess;
    }

    public void setEnableEndpointPublicAccess(Boolean enableEndpointPublicAccess) {
        this.enableEndpointPublicAccess = enableEndpointPublicAccess;
    }

    public List<String> getPublicAccessCidrs() {
        return publicAccessCidrs;
    }

    public void setPublicAccessCidrs(List<String> publicAccessCidrs) {
        this.publicAccessCidrs = publicAccessCidrs;
    }

    public List<SecurityGroupResource> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public void copyFrom(VpcConfigResponse model) {
        setEnableEndpointPrivateAccess(model.endpointPrivateAccess());
        setEnableEndpointPublicAccess(model.endpointPublicAccess());
        setPublicAccessCidrs(model.publicAccessCidrs());
        setSecurityGroups(model.securityGroupIds()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
        setSubnets(model.subnetIds()
            .stream()
            .map(s -> findById(SubnetResource.class, s))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return null;
    }

    VpcConfigRequest toVpcConfigRequest() {
        return VpcConfigRequest.builder()
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .endpointPrivateAccess(getEnableEndpointPrivateAccess())
            .endpointPublicAccess(getEnableEndpointPublicAccess())
            .securityGroupIds(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()))
            .publicAccessCidrs(getPublicAccessCidrs())
            .build();
    }
}
