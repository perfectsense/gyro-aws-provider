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

package gyro.aws.ec2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;

/**
 * Query vpc endpoint service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    endpoint-service: $(external-query aws::vpc-endpoint-service { service-name: ''})
 */
@Type("vpc-endpoint-service")
public class EndpointServiceFinder extends Ec2TaggableAwsFinder<Ec2Client, ServiceConfiguration, EndpointServiceResource> {

    private String serviceName;
    private String serviceId;
    private String serviceState;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The name of the service.
     */
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The ID of the service.
     */
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The state of the service . Valid values are ``Pending`` or ``Available`` or ``Deleting`` or ``Deleted`` or ``Failed``.
     */
    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    @Override
    protected List<ServiceConfiguration> findAllAws(Ec2Client client) {
        return client.describeVpcEndpointServiceConfigurationsPaginator().serviceConfigurations()
            .stream().collect(Collectors.toList());
    }

    @Override
    protected List<ServiceConfiguration> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcEndpointServiceConfigurationsPaginator(r ->
            r.filters(createFilters(filters))).serviceConfigurations().stream().collect(Collectors.toList());
    }
}