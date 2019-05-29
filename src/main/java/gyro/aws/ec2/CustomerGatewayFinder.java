package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CustomerGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("customer-gateway")
public class CustomerGatewayFinder extends AwsFinder<Ec2Client, CustomerGateway, CustomerGatewayResource> {

    private String bgpAsn;
    private String customerGatewayId;
    private String ipAddress;
    private String state;
    private String type;
    private Map<String, String> tag;
    private String tagKey;

    public String getBgpAsn() {
        return bgpAsn;
    }

    public void setBgpAsn(String bgpAsn) {
        this.bgpAsn = bgpAsn;
    }

    public String getCustomerGatewayId() {
        return customerGatewayId;
    }

    public void setCustomerGatewayId(String customerGatewayId) {
        this.customerGatewayId = customerGatewayId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    protected List<CustomerGateway> findAllAws(Ec2Client client) {
        return client.describeCustomerGateways().customerGateways();
    }

    @Override
    protected List<CustomerGateway> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeCustomerGateways(r -> r.filters(createFilters(filters))).customerGateways();
    }
}