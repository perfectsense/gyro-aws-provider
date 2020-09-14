package gyro.aws.codebuild;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.VpcConfig;

public class CodebuildVpcConfig extends Diffable implements Copyable<VpcConfig> {

    private List<String> securityGroupIds;
    private List<String> subnets;
    private String vpdId;

    @Updatable
    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    @Updatable
    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    @Updatable
    public String getVpdId() {
        return vpdId;
    }

    public void setVpdId(String vpdId) {
        this.vpdId = vpdId;
    }

    @Override
    public void copyFrom(VpcConfig model) {
        setSecurityGroupIds(model.securityGroupIds());
        setSubnets(model.subnets());
        setVpdId(model.vpcId());
    }

    @Override
    public String primaryKey() {
        return "vpc config";
    }
}
