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
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

public class SecurityGroupIngressRuleResource extends SecurityGroupRuleResource {
    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);
        client.authorizeSecurityGroupIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        client.updateSecurityGroupRuleDescriptionsIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        try {
            client.revokeSecurityGroupIngress(r -> r.groupId(getGroupId()).ipPermissions(getIpPermissionRequest()));
        } catch (Ec2Exception eex) {
            if (!eex.awsErrorDetails().errorCode().equals("InvalidPermission.NotFound")) {
                throw eex;
            }
        }
    }
}
