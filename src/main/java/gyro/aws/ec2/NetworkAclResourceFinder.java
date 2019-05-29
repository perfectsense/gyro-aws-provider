package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("network-acl")
public class NetworkAclResourceFinder extends AwsFinder<Ec2Client, NetworkAcl, NetworkAclResource> {
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

    @Filter("association.association-id")
    public String getAssociationAssociationId() {
        return associationAssociationId;
    }

    public void setAssociationAssociationId(String associationAssociationId) {
        this.associationAssociationId = associationAssociationId;
    }

    @Filter("association.network-acl-id")
    public String getAssociationNetworkAclId() {
        return associationNetworkAclId;
    }

    public void setAssociationNetworkAclId(String associationNetworkAclId) {
        this.associationNetworkAclId = associationNetworkAclId;
    }

    @Filter("association.subnet-id")
    public String getAssociationSubnetId() {
        return associationSubnetId;
    }

    public void setAssociationSubnetId(String associationSubnetId) {
        this.associationSubnetId = associationSubnetId;
    }

    @Filter("default")
    public String getDefaultAcl() {
        return defaultAcl;
    }

    public void setDefaultAcl(String defaultAcl) {
        this.defaultAcl = defaultAcl;
    }

    @Filter("entry.cidr")
    public String getEntryCidr() {
        return entryCidr;
    }

    public void setEntryCidr(String entryCidr) {
        this.entryCidr = entryCidr;
    }

    @Filter("entry.icmp.code")
    public String getEntryIcmpCode() {
        return entryIcmpCode;
    }

    public void setEntryIcmpCode(String entryIcmpCode) {
        this.entryIcmpCode = entryIcmpCode;
    }

    @Filter("entry.icmp.type")
    public String getEntryIcmpType() {
        return entryIcmpType;
    }

    public void setEntryIcmpType(String entryIcmpType) {
        this.entryIcmpType = entryIcmpType;
    }

    @Filter("entry.ipv6-cidr")
    public String getEntryIpv6Cidr() {
        return entryIpv6Cidr;
    }

    public void setEntryIpv6Cidr(String entryIpv6Cidr) {
        this.entryIpv6Cidr = entryIpv6Cidr;
    }

    @Filter("entry.port-range.from")
    public String getEntryPortRangeFrom() {
        return entryPortRangeFrom;
    }

    public void setEntryPortRangeFrom(String entryPortRangeFrom) {
        this.entryPortRangeFrom = entryPortRangeFrom;
    }

    @Filter("entry.port-range.to")
    public String getEntryPortRangeTo() {
        return entryPortRangeTo;
    }

    public void setEntryPortRangeTo(String entryPortRangeTo) {
        this.entryPortRangeTo = entryPortRangeTo;
    }

    @Filter("entry.protocol")
    public String getEntryProtocol() {
        return entryProtocol;
    }

    public void setEntryProtocol(String entryProtocol) {
        this.entryProtocol = entryProtocol;
    }

    @Filter("entry.rule-action")
    public String getEntryRuleAction() {
        return entryRuleAction;
    }

    public void setEntryRuleAction(String entryRuleAction) {
        this.entryRuleAction = entryRuleAction;
    }

    @Filter("entry.rule-number")
    public String getEntryRuleNumber() {
        return entryRuleNumber;
    }

    public void setEntryRuleNumber(String entryRuleNumber) {
        this.entryRuleNumber = entryRuleNumber;
    }

    public String getNetworkAclId() {
        return networkAclId;
    }

    public void setNetworkAclId(String networkAclId) {
        this.networkAclId = networkAclId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    protected List<NetworkAcl> findAllAws(Ec2Client client) {
        return client.describeNetworkAcls().networkAcls();
    }

    @Override
    protected List<NetworkAcl> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeNetworkAcls(r -> r.filters(createFilters(filters))).networkAcls();
    }
}
