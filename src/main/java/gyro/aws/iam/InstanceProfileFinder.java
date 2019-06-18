package gyro.aws.iam;

import gyro.aws.AwsFinder;
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

    /**
     * The name of the instance profile.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<InstanceProfile> findAws(IamClient client, Map<String, String> filters) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);

        List<InstanceProfile> instanceProfile = new ArrayList<>();

        instanceProfile.add(toUse.getInstanceProfile(r -> r.instanceProfileName(filters.get("name"))).instanceProfile());

        return instanceProfile;
    }

    @Override
    protected List<InstanceProfile> findAllAws(IamClient client) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);
        return client.listInstanceProfiles().instanceProfiles();
    }
}
