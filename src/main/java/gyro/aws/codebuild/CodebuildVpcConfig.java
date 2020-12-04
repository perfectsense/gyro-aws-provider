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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.ec2.VpcResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import software.amazon.awssdk.services.codebuild.model.VpcConfig;

public class CodebuildVpcConfig extends Diffable implements Copyable<VpcConfig> {

    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;
    private VpcResource vpc;

    /**
     * One or more security groups part of the configured vpc to be used with the code build.
     */
    @CollectionMax(5)
    @Updatable
    public List<SecurityGroupResource> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * One or more subnets part of the configured vpc to be used with the code build.
     */
    @CollectionMax(16)
    @Updatable
    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The vpc resource to be associated with the code build.
     */
    @Updatable
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    @Override
    public void copyFrom(VpcConfig model) {

        setSecurityGroups(null);
        if (model.securityGroupIds() != null && !model.securityGroupIds().isEmpty()) {
            List<SecurityGroupResource> securityGroups = new ArrayList<>();
            for (String id : model.securityGroupIds()) {
                securityGroups.add(findById(SecurityGroupResource.class, id));
            }
            setSecurityGroups(securityGroups);
        }

        setSubnets(null);
        if (model.subnets() != null && !model.subnets().isEmpty()) {
            List<SubnetResource> subnets = new ArrayList<>();
            for (String id : model.subnets()) {
                subnets.add(findById(SubnetResource.class, id));
            }
            setSubnets(subnets);
        }

        setVpc(null);
        if (model.vpcId() != null) {
            setVpc(findById(VpcResource.class, model.vpcId()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public VpcConfig toProjectVpcConfig() {
        return VpcConfig.builder()
            .vpcId(getVpc().getId())
            .securityGroupIds(getSecurityGroups().stream().map(group -> group.getId()).collect(Collectors.toList()))
            .subnets(getSubnets().stream().map(subnet -> subnet.getId()).collect(Collectors.toList()))
            .build();
    }
}
