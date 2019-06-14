package gyro.aws.ec2;

import gyro.core.resource.Resource;

import java.util.Set;

/**
 * Create a network ACL Egress rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::network-acl network-acl-example
 *         vpc: $(aws::vpc vpc-example-for-network-acl)
 *
 *         egress-rule
 *                 cidr-block: "0.0.0.0/32"
 *                 protocol : "1"
 *                 icmp-type : 1
 *                 icmp-code : 0
 *                 rule-action : "allow"
 *                 rule-number : 107
 *         end
 *
 *         egress-rule
 *                  cidr-block: "0.0.0.0/0"
 *                  protocol : "123"
 *                  rule-action : "deny"
 *                  rule-number : 109
 *         end
 *    end
 */
public class NetworkAclEgressRuleResource extends NetworkAclRuleResource {
    @Override
    public void create() {
        create(true);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        update(true);
    }

    @Override
    public void delete() {
        delete(true);
    }

    @Override
    public String toDisplayString() {
        return toDisplayString(true);
    }

    @Override
    public String primaryKey() {
        return String.format("%s, egress", getRuleNumber());
    }
}

