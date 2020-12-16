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

package gyro.aws.kendra;

import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.DataSourceVpcConfiguration;

public class KendraDataSourceVpcConfiguration extends Diffable implements Copyable<DataSourceVpcConfiguration> {

    private List<SubnetResource> subnets;
    private List<SecurityGroupResource> securityGroups;

    /**
     * The list of identifiers of subnets within your Amazon VPC.
     */
    @Updatable
    @Required
    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The list of identifiers of security groups within your Amazon VPC.
     */
    @Updatable
    @Required
    public List<SecurityGroupResource> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DataSourceVpcConfiguration model) {
        setSubnets(model.subnetIds().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList()));
        setSecurityGroups(model.securityGroupIds().stream().map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
    }

    public DataSourceVpcConfiguration toDataSourceVpcConfiguration() {
        return DataSourceVpcConfiguration.builder()
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .securityGroupIds(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .build();
    }
}
