package gyro.aws.ec2;

import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.Set;

public class SecurityGroupEgressRuleResource extends SecurityGroupRuleResource {
    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);
        client.authorizeSecurityGroupEgress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        client.updateSecurityGroupRuleDescriptionsEgress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        delete(client, getGroupId());
    }

    void delete(Ec2Client client, String securityGroupId) {
        try {
            client.revokeSecurityGroupEgress(r -> r.groupId(securityGroupId).ipPermissions(getIpPermissionRequest()));
        } catch (Ec2Exception eex) {
            if (!eex.awsErrorDetails().errorCode().equals("InvalidPermission.NotFound")) {
                throw eex;
            }
        }
    }

}
