/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

import java.util.Set;

import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

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
    public void create(GyroUI ui, State state) {
        create(true);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        update(true);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        delete(true);
    }

    @Override
    public String primaryKey() {
        return String.format("%s, egress", getRuleNumber());
    }
}

