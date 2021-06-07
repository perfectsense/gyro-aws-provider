/*
 * Copyright 2021, Perfect Sense.
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
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateTagSpecificationRequest;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.Tag;

public class LaunchTemplateTagSpecification extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateTagSpecification> {

    private ResourceType resourceType;
    private Map<String, String> tags;

    /**
     * The type of resource to tag.
     */
    @Required
    @ValidStrings({
        "CLIENT_VPN_ENDPOINT", "CUSTOMER_GATEWAY", "DEDICATED_HOST", "DHCP_OPTIONS",
        "EGRESS_ONLY_INTERNET_GATEWAY", "ELASTIC_IP", "ELASTIC_GPU", "EXPORT_IMAGE_TASK", "EXPORT_INSTANCE_TASK",
        "FLEET", "FPGA_IMAGE", "HOST_RESERVATION", "IMAGE", "IMPORT_IMAGE_TASK", "IMPORT_SNAPSHOT_TASK", "INSTANCE",
        "INTERNET_GATEWAY", "KEY_PAIR", "LAUNCH_TEMPLATE", "LOCAL_GATEWAY_ROUTE_TABLE_VPC_ASSOCIATION", "NATGATEWAY",
        "NETWORK_ACL", "NETWORK_INTERFACE", "NETWORK_INSIGHTS_ANALYSIS", "NETWORK_INSIGHTS_PATH", "PLACEMENT_GROUP",
        "RESERVED_INSTANCES", "ROUTE_TABLE", "SECURITY_GROUP", "SNAPSHOT", "SPOT_FLEET_REQUEST",
        "SPOT_INSTANCES_REQUEST", "SUBNET", "TRAFFIC_MIRROR_FILTER", "TRAFFIC_MIRROR_SESSION", "TRAFFIC_MIRROR_TARGET",
        "TRANSIT_GATEWAY", "TRANSIT_GATEWAY_ATTACHMENT", "TRANSIT_GATEWAY_CONNECT_PEER",
        "TRANSIT_GATEWAY_MULTICAST_DOMAIN", "TRANSIT_GATEWAY_ROUTE_TABLE", "VOLUME", "VPC", "VPC_PEERING_CONNECTION",
        "VPN_CONNECTION", "VPN_GATEWAY", "VPC_FLOW_LOG" })
    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * The tags to apply to the resource.
     */
    @Required
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateTagSpecification model) {
        setResourceType(model.resourceType());

        getTags().clear();
        if (model.hasTags()) {
            model.tags().forEach(r -> getTags().put(r.key(), r.value()));
        }
    }

    @Override
    public String primaryKey() {
        return getResourceType().toString();
    }

    LaunchTemplateTagSpecificationRequest toLaunchTemplateTagSpecificationRequest() {
        return LaunchTemplateTagSpecificationRequest.builder()
            .resourceType(getResourceType())
            .tags(getTags().entrySet()
                .stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList()))
            .build();
    }
}
