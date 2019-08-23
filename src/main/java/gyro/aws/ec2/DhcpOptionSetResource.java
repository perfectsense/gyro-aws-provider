package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeValue;
import software.amazon.awssdk.services.ec2.model.CreateDhcpOptionsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeDhcpOptionsResponse;
import software.amazon.awssdk.services.ec2.model.DhcpConfiguration;
import software.amazon.awssdk.services.ec2.model.DhcpOptions;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NewDhcpConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a DHCP option set with the specified options.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dhcp-option example-dhcp
 *         domain-name: example.com
 *         domain-name-servers: [192.168.1.1, 192.168.1.2]
 *         ntp-servers: [10.2.5.1]
 *         netbios-name-servers: [192.168.1.1, 192.168.1.2]
 *         netbios-node-type: 2
 *     end
 */

@Type("dhcp-option")
public class DhcpOptionSetResource extends Ec2TaggableResource<DhcpOptions> implements Copyable<DhcpOptions> {

    private String id;
    private String domainName;
    private Set<String> domainNameServers;
    private Set<String> ntpServers;
    private Set<String> netbiosNameServers;
    private String netbiosNodeType;

    private static final String CONFIG_DOMAIN_NAME = "domain-name";
    private static final String CONFIG_DOMAIN_NAME_SERVERS = "domain-name-servers";
    private static final String CONFIG_NTP_SERVERS = "ntp-servers";
    private static final String CONFIG_NETBIOS_SERVERS = "netbios-name-servers";
    private static final String CONFIG_NETBIOS_NODE_TYPE = "netbios-node-type";

    /**
     * The domain name for the DHCP option set.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * A list of domain name servers for the DHCP option set.
     */
    public Set<String> getDomainNameServers() {
        if (domainNameServers == null) {
            domainNameServers = new HashSet<>();
        }

        return domainNameServers;
    }

    public void setDomainNameServers(Set<String> domainNameServers) {
        this.domainNameServers = domainNameServers;
    }

    /**
     * A list of ntp servers for the DHCP option set.
     */
    public Set<String> getNtpServers() {
        if (ntpServers == null) {
            ntpServers = new HashSet<>();
        }

        return ntpServers;
    }

    public void setNtpServers(Set<String> ntpServers) {
        this.ntpServers = ntpServers;
    }

    /**
     * A list of ntp bios servers for the DHCP option set.
     */
    public Set<String> getNetbiosNameServers() {
        if (netbiosNameServers == null) {
            netbiosNameServers = new HashSet<>();
        }

        return netbiosNameServers;
    }

    public void setNetbiosNameServers(Set<String> netbiosNameServers) {
        this.netbiosNameServers = netbiosNameServers;
    }

    /**
     * The ntp bios node type for the DHCP option set.
     */
    public String getNetbiosNodeType() {
        return netbiosNodeType;
    }

    public void setNetbiosNodeType(String netbiosNodeType) {
        this.netbiosNodeType = netbiosNodeType;
    }

    /**
     * The ID of a custom DHCP option set. See `DHCP Options Sets <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_DHCP_Options.html/>`_.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(DhcpOptions dhcpOptions) {
        setId(dhcpOptions.dhcpOptionsId());

        for (DhcpConfiguration config : dhcpOptions.dhcpConfigurations()) {
            if (config.key().equals(CONFIG_DOMAIN_NAME)) {
                setDomainName(!config.values().isEmpty() ? config.values().get(0).value() : null);
            }
            if (config.key().equals(CONFIG_DOMAIN_NAME_SERVERS)) {
                setDomainNameServers(config.values().stream().map(AttributeValue::value).collect(Collectors.toSet()));
            }
            if (config.key().equals(CONFIG_NTP_SERVERS)) {
                setNtpServers(config.values().stream().map(AttributeValue::value).collect(Collectors.toSet()));
            }
            if (config.key().equals(CONFIG_NETBIOS_SERVERS)) {
                setNetbiosNameServers(config.values().stream().map(AttributeValue::value).collect(Collectors.toSet()));
            }
            if (config.key().equals(CONFIG_NETBIOS_NODE_TYPE)) {
                setNetbiosNodeType(!config.values().isEmpty() ? config.values().get(0).value() : null);
            }
        }

        refreshTags();
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
    protected void doCreate(GyroUI ui, State state) {
        Collection<NewDhcpConfiguration> configs = new ArrayList<>();
        addDhcpConfiguration(configs, CONFIG_DOMAIN_NAME, !ObjectUtils.isBlank(getDomainName()) ? Collections.singletonList(getDomainName()) : new ArrayList<>());
        addDhcpConfiguration(configs, CONFIG_DOMAIN_NAME_SERVERS, new ArrayList<>(getDomainNameServers()));
        addDhcpConfiguration(configs, CONFIG_NTP_SERVERS, new ArrayList<>(getNtpServers()));
        addDhcpConfiguration(configs, CONFIG_NETBIOS_SERVERS, new ArrayList<>(getNetbiosNameServers()));
        addDhcpConfiguration(configs, CONFIG_NETBIOS_NODE_TYPE, !ObjectUtils.isBlank(getNetbiosNodeType()) ? Collections.singletonList(getNetbiosNodeType()) : new ArrayList<>());

        Ec2Client client = createClient(Ec2Client.class);

        CreateDhcpOptionsResponse response = client.createDhcpOptions(
            r -> r.dhcpConfigurations(configs)
        );

        String optionsId = response.dhcpOptions().dhcpOptionsId();
        setId(optionsId);
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteDhcpOptions(r -> r.dhcpOptionsId(getId()));
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

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load dhcp options.");
        }

        try {
            DescribeDhcpOptionsResponse response = client.describeDhcpOptions(r -> r.dhcpOptionsIds(getId()));

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
