package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeValue;
import software.amazon.awssdk.services.ec2.model.CreateDhcpOptionsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeDhcpOptionsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeDhcpOptionsResponse;
import software.amazon.awssdk.services.ec2.model.DhcpConfiguration;
import software.amazon.awssdk.services.ec2.model.DhcpOptions;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NewDhcpConfiguration;
import software.amazon.awssdk.services.ec2.model.Vpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Creates a DHCP option set with the specified options.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dhcp-option example-dhcp
 *         domain-name: [example.com]
 *         domain-name-servers: [192.168.1.1, 192.168.1.2]
 *         ntp-servers: [10.2.5.1]
 *         netbios-name-servers: [192.168.1.1, 192.168.1.2]
 *         netbios-node-type: [2]
 *     end
 */

@Type("dhcp-option")
public class DhcpOptionSetResource extends Ec2TaggableResource<DhcpOptions> implements Copyable<DhcpOptions> {

    private String dhcpOptionsId;
    private List<String> domainName;
    private List<String> domainNameServers;
    private List<String> ntpServers;
    private List<String> netbiosNameServers;
    private List<String> netbiosNodeType;

    private static final String configDomainName = "domain-name";
    private static final String configDomainNameServers = "domain-name-servers";
    private static final String configNtpServers = "ntp-servers";
    private static final String configNetbiosServers = "netbios-name-servers";
    private static final String configNetbiosNodeType = "netbios-node-type";

    /**
     * The ID of a custom DHCP option set. See `DHCP Options Sets <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_DHCP_Options.html/>`_.
     */
    @Id
    @Output
    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    /**
     * The domain name for the DHCP option set.
     */
    public List<String> getDomainName() {
        if (domainName == null) {
            domainName = new ArrayList<>();
        }

        return domainName;
    }

    public void setDomainName(List<String> domainName) {
        this.domainName = domainName;
    }

    /**
     * A list of domain name servers for the DHCP option set.
     */
    public List<String> getDomainNameServers() {
        if (domainNameServers == null) {
            domainNameServers = new ArrayList<>();
        }

        return domainNameServers;
    }

    public void setDomainNameServers(List<String> domainNameServers) {
        this.domainNameServers = domainNameServers;
    }

    /**
     * A list of ntp servers for the DHCP option set.
     */
    public List<String> getNtpServers() {
        if (ntpServers == null) {
            ntpServers = new ArrayList<>();
        }

        return ntpServers;
    }

    public void setNtpServers(List<String> ntpServers) {
        this.ntpServers = ntpServers;
    }

    /**
     * A list of ntp bios servers for the DHCP option set.
     */
    public List<String> getNetbiosNameServers() {
        if (netbiosNameServers == null) {
            netbiosNameServers = new ArrayList<>();
        }

        return netbiosNameServers;
    }

    public void setNetbiosNameServers(List<String> netbiosNameServers) {
        this.netbiosNameServers = netbiosNameServers;
    }

    /**
     * A list of ntp bios node type for the DHCP option set.
     */
    public List<String> getNetbiosNodeType() {
        if (netbiosNodeType == null) {
            netbiosNodeType = new ArrayList<>();
        }

        return netbiosNodeType;
    }

    public void setNetbiosNodeType(List<String> netbiosNodeType) {
        this.netbiosNodeType = netbiosNodeType;
    }

    @Override
    protected String getId() {
        return getDhcpOptionsId();
    }

    public List<String> convertString(Collection<AttributeValue> toConvert) {
        List<String> convertedList = new ArrayList<>();
        for (AttributeValue str : toConvert) {
            convertedList.add(str.value());
        }
        return convertedList;
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        DhcpOptions dhcpOptions = getDhcpOptions(client);

        if (dhcpOptions == null) {
            return false;
        }

        copyFrom(dhcpOptions);

        return true;
    }

    @Override
    protected void doCreate() {
        Collection<NewDhcpConfiguration> configs = new ArrayList<>();
        addDhcpConfiguration(configs, configDomainName, getDomainName());
        addDhcpConfiguration(configs, configDomainNameServers, getDomainNameServers());
        addDhcpConfiguration(configs, configNtpServers, getNtpServers());
        addDhcpConfiguration(configs, configNetbiosServers, getNetbiosNameServers());
        addDhcpConfiguration(configs, configNetbiosNodeType, getNetbiosNodeType());

        Ec2Client client = createClient(Ec2Client.class);

        CreateDhcpOptionsResponse response = client.createDhcpOptions(
            r -> r.dhcpConfigurations(configs)
        );

        String optionsId = response.dhcpOptions().dhcpOptionsId();
        setDhcpOptionsId(optionsId);
    }

    @Override
    protected void doUpdate(AwsResource current, Set<String> changedProperties) {
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteDhcpOptions(r -> r.dhcpOptionsId(getDhcpOptionsId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("dhcp options");

        if (getDhcpOptionsId() != null) {
            sb.append(" - ").append(getDhcpOptionsId());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(DhcpOptions dhcpOptions) {
        setDhcpOptionsId(dhcpOptions.dhcpOptionsId());

        for (DhcpConfiguration config : dhcpOptions.dhcpConfigurations()) {
            if (config.key().equals(configDomainName)) {
                setDomainName(convertString(config.values()));
            }
            if (config.key().equals(configDomainNameServers)) {
                setDomainNameServers(convertString(config.values()));
            }
            if (config.key().equals(configNtpServers)) {
                setNtpServers(convertString(config.values()));
            }
            if (config.key().equals(configNetbiosServers)) {
                setNetbiosNameServers(convertString(config.values()));
            }
            if (config.key().equals(configNetbiosNodeType)) {
                setNetbiosNodeType(convertString(config.values()));
            }
        }
    }

    private void addDhcpConfiguration(Collection<NewDhcpConfiguration> dhcpConfigurations, String configName, List<String> newConfiguration) {
        if (!newConfiguration.isEmpty()) {
            NewDhcpConfiguration dhcpConfiguration = NewDhcpConfiguration.builder()
                .key(configName)
                .values(newConfiguration)
                .build();
            dhcpConfigurations.add(dhcpConfiguration);
        }
    }

    private DhcpOptions getDhcpOptions(Ec2Client client) {
        DhcpOptions dhcpOptions = null;

        if (ObjectUtils.isBlank(getDhcpOptionsId())) {
            throw new GyroException("dhcp-options-id is missing, unable to load dhcp options.");
        }

        try {
            DescribeDhcpOptionsResponse response = client.describeDhcpOptions(r -> r.dhcpOptionsIds(getDhcpOptionsId()));

            if (!response.dhcpOptions().isEmpty()) {
                dhcpOptions = response.dhcpOptions().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return dhcpOptions;
    }
}
