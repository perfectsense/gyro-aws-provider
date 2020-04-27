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
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.eks.model.VpcConfigRequest;
import software.amazon.awssdk.services.eks.model.VpcConfigResponse;

public class EksVpcConfig extends Diffable implements Copyable<VpcConfigResponse> {

    private Boolean enableEndpointPrivateAccess;
    private Boolean enableEndpointPublicAccess;
    private List<String> publicAccessCidrs;
    private List<SecurityGroupResource> securityGroups;
    private List<SubnetResource> subnets;

    /**
     * Allow private access to your cluster's Kubernetes API server endpoint. Defaults to ``true``.
     */
    public Boolean getEnableEndpointPrivateAccess() {
        return enableEndpointPrivateAccess;
    }

    public void setEnableEndpointPrivateAccess(Boolean enableEndpointPrivateAccess) {
        this.enableEndpointPrivateAccess = enableEndpointPrivateAccess;
    }

    /**
     * Allow public access to your cluster's Kubernetes API server endpoint. Defaults to ``false``.
     */
    public Boolean getEnableEndpointPublicAccess() {
        return enableEndpointPublicAccess;
    }

    public void setEnableEndpointPublicAccess(Boolean enableEndpointPublicAccess) {
        this.enableEndpointPublicAccess = enableEndpointPublicAccess;
    }

    /**
     * The CIDR blocks that are allowed access to your cluster's public Kubernetes API server endpoint. Defaults to ``0.0.0.0/0``.
     */
    public List<String> getPublicAccessCidrs() {
        return publicAccessCidrs;
    }

    public void setPublicAccessCidrs(List<String> publicAccessCidrs) {
        this.publicAccessCidrs = publicAccessCidrs;
    }

    /**
     * The security groups to use to allow communication between your worker nodes and the Kubernetes control plane.
     */
    public List<SecurityGroupResource> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The subnets for the Amazon EKS worker nodes. (Required)
     */
    @Required
    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public void copyFrom(VpcConfigResponse model) {
        setEnableEndpointPrivateAccess(model.endpointPrivateAccess());
        setEnableEndpointPublicAccess(model.endpointPublicAccess());
        setPublicAccessCidrs(model.publicAccessCidrs());
        setSecurityGroups(model.securityGroupIds()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
        setSubnets(model.subnetIds()
            .stream()
            .map(s -> findById(SubnetResource.class, s))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return null;
    }

    VpcConfigRequest toVpcConfigRequest() {
        return VpcConfigRequest.builder()
            .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .endpointPrivateAccess(getEnableEndpointPrivateAccess())
            .endpointPublicAccess(getEnableEndpointPublicAccess())
            .securityGroupIds(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .publicAccessCidrs(getPublicAccessCidrs())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getEnableEndpointPrivateAccess() == null || getEnableEndpointPrivateAccess().equals(Boolean.FALSE)) && (
            getEnableEndpointPublicAccess() != null && getEnableEndpointPublicAccess().equals(Boolean.FALSE))) {
            errors.add(new ValidationError(
                this,
                null,
                "Both 'enable-endpoint-private-access' and 'enable-endpoint-public-access' cannot be set to 'false'"));
        }

        return errors;
    }
}
