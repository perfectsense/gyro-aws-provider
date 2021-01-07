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
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;

/**
 * Query network acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    network-acl: $(external-query aws::network-acl { network-acl-id: ''})
 */
@Type("network-acl")
public class NetworkAclFinder extends Ec2TaggableAwsFinder<Ec2Client, NetworkAcl, NetworkAclResource> {

    private String associationAssociationId;
    private String associationNetworkAclId;
    private String associationSubnetId;
    private String defaultAcl;
    private String entryCidr;
    private String entryIcmpCode;
    private String entryIcmpType;
    private String entryIpv6Cidr;
    private String entryPortRangeFrom;
    private String entryPortRangeTo;
    private String entryProtocol;
    private String entryRuleAction;
    private String entryRuleNumber;
    private String networkAclId;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;
    private String vpcId;

    /**
     * The ID of an association ID for the ACL.
     */
    @Filter("association.association-id")
    public String getAssociationAssociationId() {
        return associationAssociationId;
    }

    public void setAssociationAssociationId(String associationAssociationId) {
        this.associationAssociationId = associationAssociationId;
    }

    /**
     * The ID of the network ACL involved in the association.
     */
    @Filter("association.network-acl-id")
    public String getAssociationNetworkAclId() {
        return associationNetworkAclId;
    }

    public void setAssociationNetworkAclId(String associationNetworkAclId) {
        this.associationNetworkAclId = associationNetworkAclId;
    }

    /**
     * The ID of the subnet involved in the association.
     */
    @Filter("association.subnet-id")
    public String getAssociationSubnetId() {
        return associationSubnetId;
    }

    public void setAssociationSubnetId(String associationSubnetId) {
        this.associationSubnetId = associationSubnetId;
    }

    /**
     * Indicates whether the ACL is the default network ACL for the VPC.
     */
    @Filter("default")
    public String getDefaultAcl() {
        return defaultAcl;
    }

    public void setDefaultAcl(String defaultAcl) {
        this.defaultAcl = defaultAcl;
    }

    /**
     * The IPv4 CIDR range specified in the entry.
     */
    @Filter("entry.cidr")
    public String getEntryCidr() {
        return entryCidr;
    }

    public void setEntryCidr(String entryCidr) {
        this.entryCidr = entryCidr;
    }

    /**
     * The ICMP code specified in the entry, if any.
     */
    @Filter("entry.icmp.code")
    public String getEntryIcmpCode() {
        return entryIcmpCode;
    }

    public void setEntryIcmpCode(String entryIcmpCode) {
        this.entryIcmpCode = entryIcmpCode;
    }

    /**
     * The ICMP type specified in the entry, if any.
     */
    @Filter("entry.icmp.type")
    public String getEntryIcmpType() {
        return entryIcmpType;
    }

    public void setEntryIcmpType(String entryIcmpType) {
        this.entryIcmpType = entryIcmpType;
    }

    /**
     * The IPv6 CIDR range specified in the entry.
     */
    @Filter("entry.ipv6-cidr")
    public String getEntryIpv6Cidr() {
        return entryIpv6Cidr;
    }

    public void setEntryIpv6Cidr(String entryIpv6Cidr) {
        this.entryIpv6Cidr = entryIpv6Cidr;
    }

    /**
     * The start of the port range specified in the entry.
     */
    @Filter("entry.port-range.from")
    public String getEntryPortRangeFrom() {
        return entryPortRangeFrom;
    }

    public void setEntryPortRangeFrom(String entryPortRangeFrom) {
        this.entryPortRangeFrom = entryPortRangeFrom;
    }

    /**
     * The end of the port range specified in the entry.
     */
    @Filter("entry.port-range.to")
    public String getEntryPortRangeTo() {
        return entryPortRangeTo;
    }

    public void setEntryPortRangeTo(String entryPortRangeTo) {
        this.entryPortRangeTo = entryPortRangeTo;
    }

    /**
     * The protocol specified in the entry . Valid values are ``tcp`` or ``udp`` or ``icmp`` or ``a protocol number``.
     */
    @Filter("entry.protocol")
    public String getEntryProtocol() {
        return entryProtocol;
    }

    public void setEntryProtocol(String entryProtocol) {
        this.entryProtocol = entryProtocol;
    }

    /**
     * Indicates whether to allow or deny the matching traffic . Valid values are ``allow`` or ``deny``.
     */
    @Filter("entry.rule-action")
    public String getEntryRuleAction() {
        return entryRuleAction;
    }

    public void setEntryRuleAction(String entryRuleAction) {
        this.entryRuleAction = entryRuleAction;
    }

    /**
     * The number of an entry (in other words, rule) in the set of ACL entries.
     */
    @Filter("entry.rule-number")
    public String getEntryRuleNumber() {
        return entryRuleNumber;
    }

    public void setEntryRuleNumber(String entryRuleNumber) {
        this.entryRuleNumber = entryRuleNumber;
    }

    /**
     * The ID of the network ACL.
     */
    public String getNetworkAclId() {
        return networkAclId;
    }

    public void setNetworkAclId(String networkAclId) {
        this.networkAclId = networkAclId;
    }

    /**
     * The ID of the AWS account that owns the network ACL.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    /**
     * The ID of the VPC for the network ACL.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<NetworkAcl> findAllAws(Ec2Client client) {
        return client.describeNetworkAclsPaginator().networkAcls().stream().collect(Collectors.toList());
    }

    @Override
    protected List<NetworkAcl> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeNetworkAclsPaginator(r ->
            r.filters(createFilters(filters))).networkAcls().stream().collect(Collectors.toList());
    }
}