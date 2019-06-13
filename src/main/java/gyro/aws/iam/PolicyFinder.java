package gyro.aws.iam;

import gyro.aws.AwsFinder;
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

    /**
     * The arn of the policy.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Policy> findAws(IamClient client, Map<String, String> filters) {
        IamClient toUse = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        List<Policy> policy = new ArrayList<>();

        GetPolicyResponse response = toUse.getPolicy(
                r -> r.policyArn(filters.get("arn"))
        );

        policy.add(response.policy());

        return policy;
    }

    @Override
    protected List<Policy> findAllAws(IamClient client) {
        IamClient toUse = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        return toUse.listPolicies().policies();
    }
}
