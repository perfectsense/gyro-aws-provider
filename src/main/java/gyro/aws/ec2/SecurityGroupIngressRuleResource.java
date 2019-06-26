package gyro.aws.ec2;

import gyro.core.resource.Resource;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.Set;

public class SecurityGroupIngressRuleResource extends SecurityGroupRuleResource {
    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);
        client.authorizeSecurityGroupIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedFieldNames.size() == 1 && changedFieldNames.contains("description")) {
            client.updateSecurityGroupRuleDescriptionsIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
        } else {
            current.delete();
            create();
        }

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        try {
            client.revokeSecurityGroupIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
        } catch (Ec2Exception eex) {
            if (!eex.awsErrorDetails().errorCode().equals("InvalidPermission.NotFound")) {
                throw eex;
            }
        }
    }
}
