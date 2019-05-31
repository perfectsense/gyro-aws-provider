package gyro.aws.rds;

import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.HashSet;
import java.util.Set;

public class OptionConfiguration extends Diffable {

    private String optionName;
    private Set<OptionSettings> optionSettings;
    private Integer port;
    private String version;
    private Set<SecurityGroupResource> vpcSecurityGroups;

    /**
     * The name of the option.
     */
    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    /**
     * The List of option settings to include in the option configuration.
     *
     * @subresource gyro.aws.rds.OptionSettings
     */
    @Updatable
    public Set<OptionSettings> getOptionSettings() {
        if (optionSettings == null) {
            optionSettings = new HashSet<>();
        }

        return optionSettings;
    }

    public void setOptionSettings(Set<OptionSettings> optionSettings) {
        this.optionSettings = optionSettings;
    }

    /**
     * The port of the option.
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The version of the option.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * A list of VPC security groups used for this option.
     */
    public Set<SecurityGroupResource> getVpcSecurityGroups() {
        if (vpcSecurityGroups == null) {
            vpcSecurityGroups = new HashSet<>();
        }

        return vpcSecurityGroups;
    }

    public void setVpcSecurityGroups(Set<SecurityGroupResource> vpcSecurityGroups) {
        this.vpcSecurityGroups = vpcSecurityGroups;
    }

    @Override
    public String primaryKey() {
        return getOptionName();
    }

    @Override
    public String toDisplayString() {
        return "option configuration " + getOptionName();
    }
}
