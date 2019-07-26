package gyro.aws.ec2;

import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.Set;

public class SecurityGroupIngressRuleResource extends SecurityGroupRuleResource {
    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);
        client.authorizeSecurityGroupIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void doUpdate(GyroUI ui, Context context, Resource current, Set<String> changedFieldNames) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedFieldNames.size() == 1 && changedFieldNames.contains("description")) {
            client.updateSecurityGroupRuleDescriptionsIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
        } else {
            current.delete(ui, context);
            create(ui, context);
        }

    }

    @Override
    public void delete(GyroUI ui, Context context) {
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
