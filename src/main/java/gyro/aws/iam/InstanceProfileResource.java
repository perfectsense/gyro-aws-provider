package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
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
 *     aws::iam-instance-profile ex-inst-profile
 *         name: "ex-inst-profile"
 *     end
 */
@Type("iam-instance-profile")
public class InstanceProfileResource extends AwsResource implements Copyable<InstanceProfile> {

    private String arn;
    private String name;
    private String path;
    private RoleResource role;

    /**
     * The arn of the instance profile. (Required)
     */
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
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        if (getRole() != null) {
            client.removeRoleFromInstanceProfile(r -> r.roleName(getRole().getName()).instanceProfileName(getName()));
        }

        client.deleteInstanceProfile(r -> r.instanceProfileName(getName()));
    }

}
