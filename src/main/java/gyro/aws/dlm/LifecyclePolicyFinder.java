package gyro.aws.dlm;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dlm.DlmClient;
import software.amazon.awssdk.services.dlm.model.LifecyclePolicy;
import software.amazon.awssdk.services.dlm.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query ebs snapshot lifecycle policy.
 *
 * .. code-block:: gyro
 *
 *    lifecycle-policy: $(aws::dlm-lifecycle-policy EXTERNAL/* | id = '')
 */
@Type("dlm-lifecycle-policy")
public class LifecyclePolicyFinder extends AwsFinder<DlmClient, LifecyclePolicy, LifecyclePolicyResource> {
    private String id;

    /**
     * The policy ID.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<LifecyclePolicy> findAllAws(DlmClient client) {
        List<LifecyclePolicy> lifecyclePolicies = new ArrayList<>();

        client.getLifecyclePolicies().policies().forEach(o -> lifecyclePolicies.add(client.getLifecyclePolicy(r -> r.policyId(o.policyId())).policy()));

        return lifecyclePolicies;
    }

    @Override
    protected List<LifecyclePolicy> findAws(DlmClient client, Map<String, String> filters) {
        List<LifecyclePolicy> lifecyclePolicies = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                lifecyclePolicies.add(client.getLifecyclePolicy(r -> r.policyId(filters.get("id"))).policy());
            } catch (ResourceNotFoundException ignore) {
                // ignore
            }
        }

        return lifecyclePolicies;
    }
}
