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
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.VpcConfig;

public class CodebuildVpcConfig extends Diffable implements Copyable<VpcConfig> {

    private List<String> securityGroupIds;
    private List<String> subnets;
    private String vpdId;

    /**
     * The list of security group IDs in the Amazon VPC.
     */
    @Updatable
    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * The list of subnet IDs in the Amazon VPC.
     */
    @Updatable
    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    /**
     * The ID of the Amazon VPC.
     */
    @Updatable
    public String getVpdId() {
        return vpdId;
    }

    public void setVpdId(String vpdId) {
        this.vpdId = vpdId;
    }

    @Override
    public void copyFrom(VpcConfig model) {
        setSecurityGroupIds(model.securityGroupIds());
        setSubnets(model.subnets());
        setVpdId(model.vpcId());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getSecurityGroupIds().size() > 5) {
            errors.add(new ValidationError(
                this,
                null,
                "'security-group-ids' cannot have more than 5 items."
            ));
        }

        if (getSubnets().size() > 16) {
            errors.add(new ValidationError(
                this,
                null,
                "'subnets' cannot have more than 16 items."
            ));
        }

        return errors;
    }

    public VpcConfig toProjectVpcConfig() {
        return VpcConfig.builder()
            .vpcId(getVpdId())
            .securityGroupIds(getSecurityGroupIds())
            .subnets(getSubnets())
            .build();
    }
}
