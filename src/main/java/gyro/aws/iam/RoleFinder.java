package gyro.aws.iam;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsFinder;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query IAM roles.
 *
 * .. code-block:: gyro
 *
 *    role: $(aws::iam-role EXTERNAL/* | name = '')
 */
@Type("iam-role")
public class RoleFinder extends AwsFinder<IamClient, Role, RoleResource> {

    private String name;
    private String path;

    /**
     * The name of the role.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A prefix path to search for roles.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }

    @Override
    protected List<Role> findAws(IamClient client, Map<String, String> filters) {
        List<Role> role = new ArrayList<>();

        if (filters.containsKey("name")) {
            role.add(client.getRole(r -> r.roleName(filters.get("name"))).role());
        }

        if (filters.containsKey("path")) {
            role.addAll(client.listRoles(r -> r.pathPrefix(filters.get("path")))
                .roles()
                .stream()
                .collect(Collectors.toList()));
        }

        return role;
    }

    @Override
    protected List<Role> findAllAws(IamClient client) {
        return client.listRolesPaginator().roles().stream().collect(Collectors.toList());
    }
}
