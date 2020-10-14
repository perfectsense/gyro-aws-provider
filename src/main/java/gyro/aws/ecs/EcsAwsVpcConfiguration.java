/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;

public class EcsAwsVpcConfiguration extends Diffable implements Copyable<AwsVpcConfiguration> {

    private AssignPublicIp assignPublicIp;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;

    /**
     * Option to select whether the task's elastic network interface receives a public IP address.
     */
    @Updatable
    @ValidStrings("ENABLED, DISABLED")
    public AssignPublicIp getAssignPublicIp() {
        return assignPublicIp;
    }

    public void setAssignPublicIp(AssignPublicIp assignPublicIp) {
        this.assignPublicIp = assignPublicIp;
    }

    /**
     * The security groups associated with the task or service.
     */
    @Required
    @Updatable
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The subnets associated with the task or service.
     */
    @Required
    @Updatable
    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AwsVpcConfiguration model) {
        setSubnets((model.subnets().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList())));
        setSecurityGroups(model.securityGroups()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
        setAssignPublicIp(model.assignPublicIp());
    }

    public AwsVpcConfiguration toAwsVpcConfiguration() {
        return AwsVpcConfiguration.builder()
            .assignPublicIp(getAssignPublicIp())
            .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(
                Collectors.toList()))
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(
                Collectors.toList()))
            .build();
    }
}
