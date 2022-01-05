/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.AssignPublicIp;
import software.amazon.awssdk.services.eventbridge.model.AwsVpcConfiguration;

public class VpcConfiguration extends Diffable implements Copyable<AwsVpcConfiguration> {

    private AssignPublicIp assignPublicIp;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;

    @Required
    public AssignPublicIp getAssignPublicIp() {
        return assignPublicIp;
    }

    public void setAssignPublicIp(AssignPublicIp assignPublicIp) {
        this.assignPublicIp = assignPublicIp;
    }

    @CollectionMax(5)
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    @CollectionMax(16)
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
    public void copyFrom(AwsVpcConfiguration model) {
        setAssignPublicIp(model.assignPublicIp());

        getSecurityGroups().clear();
        if (model.securityGroups() != null) {
            setSecurityGroups(model.securityGroups().stream().map(o -> findById(SecurityGroupResource.class, o)).collect(Collectors.toList()));
        }

        getSubnets().clear();
        if (model.subnets() != null) {
            setSubnets(model.subnets().stream().map(o -> findById(SubnetResource.class, o)).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected AwsVpcConfiguration toAwsVpcConfiguration() {
        AwsVpcConfiguration.Builder builder = AwsVpcConfiguration.builder().assignPublicIp(getAssignPublicIp());

        if (!getSubnets().isEmpty()) {
            builder = builder.subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()));
        }

        if (!getSecurityGroups().isEmpty()) {
            builder = builder.securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()));
        }

        return builder.build();
    }
}
