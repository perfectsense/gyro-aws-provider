package gyro.aws.iam;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsFinder;

import gyro.aws.AwsResource;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query iam roles.
 *
 * .. code-block:: gyro
 *
 *    role: $(aws::role EXTERNAL/* | name = '')
 */
@Type("role")
public class RoleFinder extends AwsFinder<IamClient, Role, RoleResource> {

    private String name;

    /**
     * The name of the role.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Role> findAws(IamClient client, Map<String, String> filters) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);

        List<Role> role = new ArrayList<>();
        role.add(client.getRole(r -> r.roleName(filters.get("name"))).role());

        return role;
    }

    @Override
    protected List<Role> findAllAws(IamClient client) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);
        return client.listRoles().roles();
    }
}
