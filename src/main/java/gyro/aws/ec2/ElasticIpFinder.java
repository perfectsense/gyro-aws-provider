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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query elastic ip.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    elastic-ip: $(external-query aws::elastic-ip { allocation-id: ''})
 */
@Type("elastic-ip")
public class ElasticIpFinder extends Ec2TaggableAwsFinder<Ec2Client, Address, ElasticIpResource> {

    private String allocationId;
    private String associationId;
    private String domain;
    private String instanceId;
    private String networkInterfaceId;
    private String networkInterfaceOwnerId;
    private String privateIpAddress;
    private String publicIp;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The allocation ID for the address.
     */
    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    /**
     * The association ID for the address.
     */
    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    /**
     * Indicates whether the address is for use in EC2-Classic (standard) or in a VPC (vpc).
     */
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * The ID of the instance the address is associated with, if any.
     */
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The ID of the network interface that the address is associated with, if any.
     */
    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    /**
     * The AWS account ID of the owner.
     */
    public String getNetworkInterfaceOwnerId() {
        return networkInterfaceOwnerId;
    }

    public void setNetworkInterfaceOwnerId(String networkInterfaceOwnerId) {
        this.networkInterfaceOwnerId = networkInterfaceOwnerId;
    }

    /**
     * The private IP address associated with the Elastic IP address.
     */
    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    /**
     * The Elastic IP address.
     */
    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
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
    protected List<Address> findAllAws(Ec2Client client) {
        return client.describeAddresses().addresses();
    }

    @Override
    protected List<Address> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeAddresses(r -> r.filters(createFilters(filters))).addresses();
    }
}