package gyro.aws.ec2;

import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

/**
 * Create a network ACL Ingress rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::network-acl network-acl-example
 *         vpc: $(aws::vpc vpc-example-for-network-acl)
 *
 *         ingress-rule
 *                 cidr-block: "0.0.0.0/0"
 *                 protocol : "6"
 *                 rule-action : "allow"
 *                 rule-number : 103
 *                 from-port: 80
 *                 to-port: 80
 *         end
 *
 *         ingress-rule
 *                 cidr-block: "0.0.0.0/0"
 *                 protocol : "23"
 *                 rule-action : "deny"
 *                 rule-number : 105
 *         end
 *    end
 *
 */
public class NetworkAclIngressRuleResource extends NetworkAclRuleResource {
    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public String primaryKey() {
        return String.format("%s, ingress", getRuleNumber());
    }
}
