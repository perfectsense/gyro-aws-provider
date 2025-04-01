/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.VPCDerivedInfo;
import software.amazon.awssdk.services.opensearch.model.VPCOptions;

public class OpenSearchVpcOptions extends Diffable implements Copyable<VPCDerivedInfo> {

    private Set<SubnetResource> subnets;
    private Set<SecurityGroupResource> securityGroups;

    /**
     * The list of subnets in the same region for the VPC endpoint. One subnet per availability zone.
     */
    @Required
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The list if security groups for the VPC endpoint that need to access the domain.
     */
    @Required
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    @Override
    public void copyFrom(VPCDerivedInfo model) {
        setSecurityGroups(model.securityGroupIds()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(
                Collectors.toSet()));
        setSubnets(model.subnetIds()
            .stream()
            .map(s -> findById(SubnetResource.class, s))
            .collect(Collectors.toSet()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    VPCOptions toVPCOptions() {
        return VPCOptions.builder()
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()))
            .securityGroupIds(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toSet()))
            .build();
    }
}
