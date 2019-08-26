package gyro.aws.iam;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query IAM policies.
 *
 * .. code-block:: gyro
 *
 *    policy: $(external-query aws::iam-policy { arn: ''})
 */
@Type("iam-policy")
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
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }

    @Override
    protected List<Policy> findAws(IamClient client, Map<String, String> filters) {
        List<Policy> policy = new ArrayList<>();

        if (filters.containsKey("arn")) {
            policy.add(client.getPolicy(r -> r.policyArn(filters.get("arn"))).policy());
        }

        if (filters.containsKey("path")) {
            policy.addAll(client.listPoliciesPaginator(r -> r.pathPrefix(filters.get("path"))).policies().stream().collect(Collectors.toList()));
        }

        return policy;
    }

    @Override
    protected List<Policy> findAllAws(IamClient client) {
        return client.listPoliciesPaginator().policies().stream().collect(Collectors.toList());
    }
}
