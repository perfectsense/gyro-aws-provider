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

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CustomerGateway;

/**
 * Query customer gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    customer-gateway: $(external-query aws::customer-gateway { state: 'available'})
 */
@Type("customer-gateway")
public class CustomerGatewayFinder extends Ec2TaggableAwsFinder<Ec2Client, CustomerGateway, CustomerGatewayResource> {

    private String bgpAsn;
    private String customerGatewayId;
    private String ipAddress;
    private String state;
    private String type;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The customer gateway's Border Gateway Protocol (BGP) Autonomous System Number (ASN).
     */
    public String getBgpAsn() {
        return bgpAsn;
    }

    public void setBgpAsn(String bgpAsn) {
        this.bgpAsn = bgpAsn;
    }

    /**
     * The ID of the customer gateway.
     */
    public String getCustomerGatewayId() {
        return customerGatewayId;
    }

    public void setCustomerGatewayId(String customerGatewayId) {
        this.customerGatewayId = customerGatewayId;
    }

    /**
     * The IP address of the customer gateway's Internet-routable external interface.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The state of the customer gateway . Valid values are ``pending`` or ``available`` or ``deleting`` or ``deleted``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The type of customer gateway. Currently, the only supported type is ``ipsec.1``.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
    protected List<CustomerGateway> findAllAws(Ec2Client client) {
        return client.describeCustomerGateways().customerGateways();
    }

    @Override
    protected List<CustomerGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeCustomerGateways(r -> r.filters(createFilters(filters))).customerGateways();
    }
}
