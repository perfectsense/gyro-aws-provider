package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;

import gyro.core.resource.Output;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
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
 *         allow-re-association : true
 *         instance : $(aws::instance instance-example)
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::elastic-ip elastic-ip-example
 *         public-ip: 52.20.183.53
 *         allow-re-association : true
 *         network-interface : $(aws::network-interface network-interface-example)
 *     end
 */
@Type("elastic-ip")
public class ElasticIpResource extends Ec2TaggableResource<Address> implements Copyable<Address> {

    private String id;
    private String publicIp;
    private Boolean isStandardDomain;
    private InstanceResource instance;
    private NetworkInterfaceResource networkInterface;
    private String associationId;
    private Boolean allowReAssociation;
    private String networkInterfaceAssociationPrivateIp;

    /**
     * Requested public ip for acquirement. See `Elastic IP <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html/>`_.
     */
    @Output
    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    /**
     * Network Interface required when the requested public ip is associated with a Network Interface.
     */
    @Updatable
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * Instance required when the requested public ip is associated with an Instance.
     */
    @Updatable
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
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
    public Boolean getAllowReAssociation() {
        if (allowReAssociation == null) {
            allowReAssociation = false;
        }

        return allowReAssociation;
    }

    public void setAllowReAssociation(Boolean allowReAssociation) {
        this.allowReAssociation = allowReAssociation;
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

    /**
     * The private ip of the Network Interface being attached to.
     */
    public String getNetworkInterfaceAssociationPrivateIp() {
        return networkInterfaceAssociationPrivateIp;
    }

    public void setNetworkInterfaceAssociationPrivateIp(String networkInterfaceAssociationPrivateIp) {
        this.networkInterfaceAssociationPrivateIp = networkInterfaceAssociationPrivateIp;
    }

    /**
     * Allocation ID when the requested public ip is acquired.
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
    public void copyFrom(Address address) {
        setId(address.allocationId());
        setIsStandardDomain(address.domain().equals(DomainType.STANDARD));
        setPublicIp(address.publicIp());
        setNetworkInterface(!ObjectUtils.isBlank(address.networkInterfaceId()) ? findById(NetworkInterfaceResource.class, address.networkInterfaceId()) : null);
        setInstance(!ObjectUtils.isBlank(address.instanceId()) ? findById(InstanceResource.class, address.instanceId()) : null);
        setAssociationId(address.associationId());
    }

    @Override
    public boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private Address getAddress(Ec2Client client) {
        Address address = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load elastic-ip.");
        }

        try {
            DescribeAddressesResponse response = client.describeAddresses(
                r -> r.allocationIds(Collections.singleton(getId()))
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
