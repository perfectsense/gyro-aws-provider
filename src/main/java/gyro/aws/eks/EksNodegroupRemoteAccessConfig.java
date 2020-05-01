/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.eks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.KeyPairResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.RemoteAccessConfig;

public class EksNodegroupRemoteAccessConfig extends Diffable implements Copyable<RemoteAccessConfig> {

    private KeyPairResource ec2SshKey;
    private List<SecurityGroupResource> sourceSecurityGroups;

    /**
     * The SSH key that provides access for communication with the worker nodes in the managed node group. (Required)
     */
    @Required
    public KeyPairResource getEc2SshKey() {
        return ec2SshKey;
    }

    public void setEc2SshKey(KeyPairResource ec2SshKey) {
        this.ec2SshKey = ec2SshKey;
    }

    /**
     * The security groups that are allowed SSH access to the worker nodes.
     */
    public List<SecurityGroupResource> getSourceSecurityGroups() {
        if (sourceSecurityGroups == null) {
            sourceSecurityGroups = new ArrayList<>();
        }

        return sourceSecurityGroups;
    }

    public void setSourceSecurityGroups(List<SecurityGroupResource> sourceSecurityGroups) {
        this.sourceSecurityGroups = sourceSecurityGroups;
    }

    @Override
    public void copyFrom(RemoteAccessConfig model) {
        setEc2SshKey(findById(KeyPairResource.class, model.ec2SshKey()));
        setSourceSecurityGroups(model.sourceSecurityGroups()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    RemoteAccessConfig toRemoteAccessConfig() {
        return RemoteAccessConfig.builder()
            .ec2SshKey(getEc2SshKey().getName())
            .sourceSecurityGroups(getSourceSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .build();
    }
}
