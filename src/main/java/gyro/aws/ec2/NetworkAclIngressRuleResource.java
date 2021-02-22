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
        create(false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        update(false);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        delete(false);
    }

    @Override
    public String primaryKey() {
        return String.format("%s, ingress", getRuleNumber());
    }
}
