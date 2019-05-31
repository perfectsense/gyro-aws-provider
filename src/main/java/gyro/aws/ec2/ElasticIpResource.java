package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;

import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.AllocateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AssociateAddressResponse;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesResponse;
import software.amazon.awssdk.services.ec2.model.DomainType;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.MoveAddressToVpcResponse;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

/**
 * Creates an Elastic IP with the specified Public IP.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::elastic-ip elastic-ip-example
 *         public-ip: 52.20.183.53
 *         allow-reassociation : true
 *         instance : $(aws::instance instance-example)
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::elastic-ip elastic-ip-example
 *         public-ip: 52.20.183.53
 *         allow-reassociation : true
 *         network-interface : $(aws::network-interface network-interface-example)
 *     end
 */
@Type("elastic-ip")
public class ElasticIpResource extends Ec2TaggableResource<Address> implements Copyable<Address> {

    private String allocationId;
    private String publicIp;
    private Boolean isStandardDomain;
    private InstanceResource instance;
    private NetworkInterfaceResource networkInterface;
    private String associationId;
    private Boolean allowReassociation;

    /**
     * Requested public ip for acquirement. See `Elastic IP <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html/>`_.
     */
    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    /**
     * Network Interface id required when the requested public ip is associated with a network interface.
     */
    @Updatable
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * Instance id required when the requested public ip is associated with an instance.
     */
    @Updatable
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Allocation id when the requested public ip is acquired.
     */
    @Id
    @Output
    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    /**
     * Association id assigned when the requested public ip is associated to an instance or a network interface.
     */
    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    /**
     * Allows reassociation of elastic Ip with another resource.
     */
    @Updatable
    public Boolean getAllowReassociation() {
        return allowReassociation;
    }

    public void setAllowReassociation(Boolean allowReassociation) {
        this.allowReassociation = allowReassociation;
    }

    /**
     * Set standard domain. Defaults to false.
     */
    @Updatable
    public Boolean getIsStandardDomain() {
        if (isStandardDomain == null) {
            isStandardDomain = false;
        }

        return isStandardDomain;
    }

    public void setIsStandardDomain(Boolean isStandardDomain) {
        this.isStandardDomain = isStandardDomain;
    }

    @Override
    protected String getId() {
        return getAllocationId();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Address address = getAddress(client);

        if (address == null) {
            return false;
        }

        copyFrom(address);

        return true;
    }

    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        try {
            AllocateAddressResponse response = client.allocateAddress(
                r -> r.address(getPublicIp())
                    .domain(getIsStandardDomain() ? DomainType.STANDARD : DomainType.VPC)
            );
            setAllocationId(response.allocationId());
            setPublicIp(response.publicIp());
            if (getInstance() != null || getNetworkInterface() != null) {
                GyroCore.ui().write("\n@|bold,blue Skipping association of elastic IP"
                    + ", must be updated to associate with a resource|@");
            }
        } catch (Ec2Exception eex) {
            if (eex.awsErrorDetails().errorCode().equals("InvalidAddress.NotFound")) {
                throw new GyroException(MessageFormat.format("Elastic Ip - {0} Unavailable/Not found.", getPublicIp()));
            } else if (eex.awsErrorDetails().errorCode().equals("AddressLimitExceeded")) {
                throw new GyroException("The maximum number of addresses has been reached.");
            }
        }
    }

    @Override
    public void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("is-standard-domain")) {
            if (!getIsStandardDomain()) {
                MoveAddressToVpcResponse response = client.moveAddressToVpc(r -> r.publicIp(getPublicIp()));
                setAllocationId(response.allocationId());
                setIsStandardDomain(false);
            } else {
                throw new GyroException(MessageFormat.format("Elastic Ip - {0}, VPC domain to Standard domain not feasible. ", getPublicIp()));
            }
        }

        if (changedProperties.contains("instance") || changedProperties.contains("network-interface")) {
            if (!getAllowReassociation()) {
                throw new GyroException("Please set the allow re-association to true in order for any associations.");
            }

            if (changedProperties.contains("instance")) {
                if (getInstance() != null) {
                    AssociateAddressResponse resp = client.associateAddress(r -> r.allocationId(getAllocationId())
                        .instanceId(getInstance().getInstanceId())
                        .allowReassociation(getAllowReassociation()));
                    setAssociationId(resp.associationId());
                } else {
                    if (!changedProperties.contains("network-interfac")) {
                        client.disassociateAddress(r -> r.associationId(getAssociationId()));
                    }
                }
            }

            if (changedProperties.contains("network-interface")) {
                if (getNetworkInterface() != null) {
                    AssociateAddressResponse resp = client.associateAddress(r -> r.allocationId(getAllocationId())
                        .networkInterfaceId(getNetworkInterface().getNetworkInterfaceId())
                        .allowReassociation(getAllowReassociation()));
                    setAssociationId(resp.associationId());
                } else {
                    if (!changedProperties.contains("instance")) {
                        client.disassociateAddress(r -> r.associationId(getAssociationId()));
                    }
                }
            }
        }

        doRefresh();
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        Address address = getAddress(client);

        try {
            if (address != null && address.associationId() != null) {
                client.disassociateAddress(r -> r.associationId(getAssociationId()));
            }
        } catch (Ec2Exception e) {
            throw new GyroException("Non managed associated resource");
        }

        try {
            client.releaseAddress(r -> r.allocationId(getAllocationId()));
        } catch (Ec2Exception eex) {
            if (eex.awsErrorDetails().errorCode().equals("InvalidAllocationID.NotFound")) {
                throw new GyroException(MessageFormat.format("Elastic Ip - {0} not found.", getPublicIp()));
            } else {
                throw eex;
            }
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("elastic ip");

        if (getPublicIp() != null && !getPublicIp().isEmpty()) {
            sb.append(" - ").append(getPublicIp());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(Address address) {
        setAllocationId(address.allocationId());
        setIsStandardDomain(address.domain().equals(DomainType.STANDARD));
        setPublicIp(address.publicIp());
        setNetworkInterface(findById(NetworkInterfaceResource.class, address.networkInterfaceId()));
        setInstance(findById(InstanceResource.class, address.instanceId()));
        setAssociationId(address.associationId());
    }

    private Address getAddress(Ec2Client client) {
        Address address = null;

        if (ObjectUtils.isBlank(getAllocationId())) {
            throw new GyroException("allocation-id is missing, unable to load elastic-ip.");
        }

        try {
            DescribeAddressesResponse response = client.describeAddresses(
                r -> r.allocationIds(Collections.singleton(getAllocationId()))
            );

            if (!response.addresses().isEmpty()) {
                address = response.addresses().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return address;
    }
}
