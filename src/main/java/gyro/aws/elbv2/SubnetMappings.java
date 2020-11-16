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

package gyro.aws.elbv2;

import gyro.aws.ec2.ElasticIpResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.resource.Diffable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.SubnetMapping;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     subnet-mapping
 *         ip-address: $(aws::elastic-ip elastic-ip-example)
 *         subnet: $(aws::subnet subnet-example)
 *     end
 */
public class SubnetMappings extends Diffable {

    private ElasticIpResource ipAddress;
    private SubnetResource subnet;

    /**
     *  The elastic ip associated with the nlb.
     */
    public ElasticIpResource getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(ElasticIpResource ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     *  The subnet associated with the nlb.
     */
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getIpAddress() != null ? getIpAddress().getId() : null, getSubnet().getId());
    }

    public SubnetMapping toSubnetMappings() {
        return SubnetMapping.builder()
                .allocationId(getIpAddress() != null ? getIpAddress().getId() : null)
                .subnetId(getSubnet() != null ? getSubnet().getId() : null)
                .build();
    }
}
