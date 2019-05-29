package gyro.aws.ec2;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DhcpOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("dhcp-options")
public class DhcpOptionSetFinder extends AwsFinder<Ec2Client, DhcpOptions, DhcpOptionSetResource> {

    private String dhcpOptionsId;
    private String key;
    private String value;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;

    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    @Override
    protected List<DhcpOptions> findAllAws(Ec2Client client) {
        return client.describeDhcpOptions().dhcpOptions();
    }

    @Override
    protected List<DhcpOptions> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeDhcpOptions(r -> r.filters(createFilters(filters))).dhcpOptions();
    }
}
