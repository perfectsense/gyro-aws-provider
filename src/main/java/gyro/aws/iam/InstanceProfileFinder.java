package gyro.aws.iam;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.InstanceProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query instance profiles.
 *
 * .. code-block:: gyro
 *
 *    instance-profile: $(aws::instance-profile EXTERNAL/* | name = '')
 */
@Type("instance-profile")
public class InstanceProfileFinder extends AwsFinder<IamClient, InstanceProfile, InstanceProfileResource> {

    private String name;
    private String path;

    /**
     * The name of the instance profile.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A prefix path to search for instance profiles.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected List<InstanceProfile> findAws(IamClient client, Map<String, String> filters) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);

        List<InstanceProfile> instanceProfile = new ArrayList<>();

        if (filters.containsKey("name")) {
            instanceProfile.add(client.getInstanceProfile(r -> r.instanceProfileName(filters.get("name"))).instanceProfile());
        }

        if (filters.containsKey("path")) {
            instanceProfile.addAll(client.listInstanceProfiles(r -> r.pathPrefix(filters.get("path"))).instanceProfiles());
        }

        return instanceProfile;
    }

    @Override
    protected List<InstanceProfile> findAllAws(IamClient client) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);
        return client.listInstanceProfiles().instanceProfiles();
    }
}
