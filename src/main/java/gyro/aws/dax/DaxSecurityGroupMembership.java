package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.SecurityGroupMembership;

public class DaxSecurityGroupMembership extends Diffable implements Copyable<SecurityGroupMembership> {

    private String securityGroupIdentifier;
    private String status;

    public String getSecurityGroupIdentifier() {
        return securityGroupIdentifier;
    }

    public void setSecurityGroupIdentifier(String securityGroupIdentifier) {
        this.securityGroupIdentifier = securityGroupIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void copyFrom(SecurityGroupMembership model) {
        setSecurityGroupIdentifier(model.securityGroupIdentifier());
        setStatus(model.status());
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getSecurityGroupIdentifier());
    }
}
