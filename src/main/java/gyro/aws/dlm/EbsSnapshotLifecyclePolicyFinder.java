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
 *    ebs-snapshot-lifecycle-policy: $(aws::ebs-snapshot-lifecycle-policy EXTERNAL/* | policy-id = '')
 */
@Type("ebs-snapshot-lifecycle-policy")
public class EbsSnapshotLifecyclePolicyFinder extends AwsFinder<DlmClient, LifecyclePolicy, EbsSnapshotLifecyclePolicyResource> {
    private String policyId;

    /**
     * The policy ID.
     */
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
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

        if (filters.containsKey("policy-id") && !ObjectUtils.isBlank(filters.get("policy-id"))) {
            try {
                lifecyclePolicies.add(client.getLifecyclePolicy(r -> r.policyId(filters.get("policy-id"))).policy());
            } catch (ResourceNotFoundException ignore) {
                // ignore
            }
        }

        return lifecyclePolicies;
    }
}