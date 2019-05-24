package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;

import java.util.List;
import java.util.Map;

@Type("elastic-ip")
public class ElasticIpResourceFinder extends AwsFinder<Ec2Client, Address, ElasticIpResource> {
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

    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    public String getNetworkInterfaceOwnerId() {
        return networkInterfaceOwnerId;
    }

    public void setNetworkInterfaceOwnerId(String networkInterfaceOwnerId) {
        this.networkInterfaceOwnerId = networkInterfaceOwnerId;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public Map<String, String> getTag() {
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

    @Override
    protected List<Address> findAllAws(Ec2Client client) {
        return client.describeAddresses().addresses();
    }

    @Override
    protected List<Address> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeAddresses(r -> r.filters(createFilters(filters))).addresses();
    }
}
