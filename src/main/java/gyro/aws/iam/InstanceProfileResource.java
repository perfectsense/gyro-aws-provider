package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateInstanceProfileResponse;
import software.amazon.awssdk.services.iam.model.GetInstanceProfileResponse;
import software.amazon.awssdk.services.iam.model.InstanceProfile;

import java.util.Set;

/**
 * Creates a Instance Profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::instance-profile ex-inst-profile
 *         name: "ex-inst-profile"
 *     end
 */
@Type("instance-profile")
public class InstanceProfileResource extends AwsResource implements Copyable<InstanceProfile> {

    private String arn;
    private String name;
    private String path;
    private RoleResource role;

    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The name of the instance profile. (Required)
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The path to the instance profile. Defaults to ``/``. (Optional)
     */
    public String getPath() {
        if (path == null) {
            path = "/";
        }

        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The role associated with the instance profile. (Optional)
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    @Override
    public void copyFrom(InstanceProfile instanceProfile) {
        setArn(instanceProfile.arn());

        if (!instanceProfile.roles().isEmpty()) {
            findById(RoleResource.class, instanceProfile.roles().get(0).arn());
        }
    }

    @Override
    public boolean refresh() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        GetInstanceProfileResponse response = client.getInstanceProfile(r -> r.instanceProfileName(getName()));

        if (response != null) {
            this.copyFrom(response.instanceProfile());

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        CreateInstanceProfileResponse response =
                client.createInstanceProfile(r -> r.instanceProfileName(getName()).path(getPath()));

        setArn(response.instanceProfile().arn());

        if (getRole() != null) {
            client.addRoleToInstanceProfile(r -> r.instanceProfileName(getName()).roleName(getRole().getName()));
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        client.removeRoleFromInstanceProfile(r -> r.roleName(getRole().getName()).instanceProfileName(getName()));

        client.deleteInstanceProfile(r -> r.instanceProfileName(getName()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append("instance profile " + getName());

        } else {
            sb.append("instance profile ");
        }

        return sb.toString();
    }
}
