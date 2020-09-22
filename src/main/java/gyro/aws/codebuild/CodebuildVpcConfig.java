package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
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
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getSecurityGroupIds().size() > 5) {
            errors.add(new ValidationError(
                this,
                null,
                "'security-group-ids' cannot have more than 5 items."
            ));
        }

        if (getSubnets().size() > 16) {
            errors.add(new ValidationError(
                this,
                null,
                "'subnets' cannot have more than 16 items."
            ));
        }

        return errors;
    }

    public VpcConfig toProjectVpcConfig() {
        return VpcConfig.builder()
            .vpcId(getVpdId())
            .securityGroupIds(getSecurityGroupIds())
            .subnets(getSubnets())
            .build();
    }
}
