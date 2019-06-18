package gyro.aws.iam;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query policies.
 *
 * .. code-block:: gyro
 *
 *    policy: $(aws::policy EXTERNAL/* | arn = '')
 */
@Type("policy")
public class PolicyFinder extends AwsFinder<IamClient, Policy, PolicyResource> {

    private String arn;
    private String path;

    /**
     * The arn of the policy.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A prefix path to search for policies.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected List<Policy> findAws(IamClient client, Map<String, String> filters) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);

        List<Policy> policy = new ArrayList<>();

        if (filters.containsKey("arn")) {
            policy.add(client.getPolicy(r -> r.policyArn(filters.get("arn"))).policy());
        }

        if (filters.containsKey("path")) {
            policy.addAll(client.listPolicies(r -> r.pathPrefix(filters.get("path"))).policies());
        }

        return policy;
    }

    @Override
    protected List<Policy> findAllAws(IamClient client) {
        client = AwsResource.createClient(IamClient.class, credentials(AwsCredentials.class), Region.AWS_GLOBAL.toString(), null);
        return client.listPolicies().policies();
    }
}
