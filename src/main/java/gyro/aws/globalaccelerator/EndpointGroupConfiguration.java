/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointConfiguration;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointDescription;

public class EndpointGroupConfiguration extends Diffable implements Copyable<EndpointDescription> {

    private Boolean clientIpPreservationEnabled;
    private String endpointId;
    private Integer weight;

    @Override
    public void copyFrom(EndpointDescription configuration) {
        setClientIpPreservationEnabled(configuration.clientIPPreservationEnabled());
        setWeight(configuration.weight());
        setEndpointId(configuration.endpointId());
    }

    @Override
    public String primaryKey() {
        return getEndpointId();
    }

    /**
     * Whether client ip preservation is enabled for an ALB endpoint.
     */
    public Boolean getClientIpPreservationEnabled() {
        return clientIpPreservationEnabled;
    }

    public void setClientIpPreservationEnabled(Boolean clientIpPreservationEnabled) {
        this.clientIpPreservationEnabled = clientIpPreservationEnabled;
    }

    /**
     * The ID for the endpoint. For NLB/ALB endpoints use their ARN. For EIPs use the allocation id. For EC2 instances use the instance id.
     */
    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    /**
     *  The weight to associate with this endpoints.
     */
    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    EndpointConfiguration endpointConfiguration() {
        return EndpointConfiguration.builder()
            .endpointId(getEndpointId())
            .weight(getWeight())
            .clientIPPreservationEnabled(getClientIpPreservationEnabled())
            .build();
    }
}
