package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.query.EqualsQueryFilter;
import gyro.lang.query.OrQueryFilter;
import gyro.lang.query.QueryFilter;
import gyro.lang.query.ResourceQuery;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.CreateSubnetRequest;
import software.amazon.awssdk.services.ec2.model.CreateSubnetResponse;
import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.ModifySubnetAttributeRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a subnet in a VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::subnet example-subnet
 *         vpc-id: $(aws::vpc example-vpc | vpc-id)
 *         availability-zone: us-east-1a
 *         cidr-block: 10.0.0.0/24
 *     end
 */
@ResourceName("subnet")
public class SubnetResource extends Ec2TaggableResource<Subnet> implements ResourceQuery<SubnetResource> {

    private String vpcId;
    private String cidrBlock;
    private String availabilityZone;
    private Boolean mapPublicIpOnLaunch;
    private String subnetId;

    public SubnetResource() {

    }

    public SubnetResource(Subnet subnet) {
        setSubnetId(subnet.subnetId());
        setCidrBlock(subnet.cidrBlock());
        setAvailabilityZone(subnet.availabilityZone());
        setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
        setVpcId(subnet.vpcId());
    }

    /**
     * The ID of the VPC to create the subnet in. (Required)
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * The IPv4 network range for the subnet, in CIDR notation. (Required)
     */
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The name of the availablity zone to create this subnet (ex. ``us-east-1a``).
     */
    @ResourceDiffProperty
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Assign a public IPv4 address to network interfaces created in this subnet.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getMapPublicIpOnLaunch() {
        return mapPublicIpOnLaunch;
    }

    public void setMapPublicIpOnLaunch(Boolean mapPublicIpOnLaunch) {
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
    }

    @ResourceOutput
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getId() {
        return getSubnetId();
    }

    private static List<String> FilterableAttributes = Arrays.asList(
        "availability-zone", "availability-zone-id", "available-ip-address-count", "cidr-block", "default-for-az",
        "ipv6-cidr-block-association.ipv6-cidr-block", "ipv6-cidr-block-association.association-id", "ipv6-cidr-block-association.state",
        "owner-id", "state", "subnet-arn", "subnet-id", "tag:*", "tag-key", "vpc-id"
    );

    private Filter handleEqualsQueryFilters(EqualsQueryFilter queryFilter) {
        if (!FilterableAttributes.contains(queryFilter.getField())) {
            return null;
        }

        return Filter.builder().name(queryFilter.getField()).values(queryFilter.getValue()).build();
    }

    @Override
    public List<SubnetResource> query(List<QueryFilter> filters) {
        Ec2Client client = createClient(Ec2Client.class);

        List<Filter> subnetFilters = new ArrayList<>();
        for (Iterator<QueryFilter> i = filters.iterator(); i.hasNext();) {
            QueryFilter filter = i.next();

            if (filter instanceof EqualsQueryFilter) {
                Filter apiFilter = handleEqualsQueryFilters((EqualsQueryFilter) filter);

                if (apiFilter != null) {
                    subnetFilters.add(apiFilter);
                    i.remove();
                }
            } else if (filter instanceof OrQueryFilter) {
                OrQueryFilter queryFilter = (OrQueryFilter) filter;

                if (queryFilter.getLeftFilter() instanceof EqualsQueryFilter) {
                    Filter apiFilter = handleEqualsQueryFilters((EqualsQueryFilter) queryFilter.getLeftFilter());

                    if (apiFilter != null) {
                        subnetFilters.add(apiFilter);
                    }
                }

                if (queryFilter.getRightFilter() instanceof EqualsQueryFilter) {
                    Filter apiFilter = handleEqualsQueryFilters((EqualsQueryFilter) queryFilter.getRightFilter());

                    if (apiFilter != null) {
                        subnetFilters.add(apiFilter);
                    }
                }


            }
        }

        if (subnetFilters.isEmpty()) {
            return new ArrayList<>();
        }

        DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
            .filters(subnetFilters)
            .build();

        List<SubnetResource> resources = client.describeSubnets(request).subnets()
            .stream()
            .map(s -> new SubnetResource(s))
            .collect(Collectors.toList());

        return resources;
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        if (ObjectUtils.isBlank(getSubnetId())) {
            throw new BeamException("subnet-id is missing, unable to load subnet.");
        }

        try {
            DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
                .subnetIds(getSubnetId())
                .build();

            for (Subnet subnet : client.describeSubnets(request).subnets()) {
                setSubnetId(subnet.subnetId());
                setAvailabilityZone(subnet.availabilityZone());
                setCidrBlock(subnet.cidrBlock());
                setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
            }
        } catch (Ec2Exception ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                return false;
            }

            throw ex;
        }

        return true;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSubnetRequest request = CreateSubnetRequest.builder()
                .availabilityZone(getAvailabilityZone())
                .cidrBlock(getCidrBlock())
                .vpcId(getVpcId())
                .build();

        CreateSubnetResponse response = client.createSubnet(request);

        setSubnetId(response.subnet().subnetId());

        modifyAttribute(client);
    }

    @Override
    protected void doUpdate(AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        modifyAttribute(client);
    }

    private void modifyAttribute(Ec2Client client) {
        if (getMapPublicIpOnLaunch() != null) {
            ModifySubnetAttributeRequest request = ModifySubnetAttributeRequest.builder()
                    .subnetId(getSubnetId())
                    .mapPublicIpOnLaunch(AttributeBooleanValue.builder().value(getMapPublicIpOnLaunch()).build())
                    .build();

            client.modifySubnetAttribute(request);
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        // Network interfaces may still be detaching, so check and wait
        // before deleting the subnet.
        while (true) {
            DescribeNetworkInterfacesRequest request = DescribeNetworkInterfacesRequest.builder()
                    .filters(Filter.builder()
                            .name("subnet-id")
                            .values(getSubnetId()).build())
                    .build();

            if (client.describeNetworkInterfaces(request).networkInterfaces().isEmpty()) {
                break;
            }

            try {
                Thread.sleep(1000);

            } catch (InterruptedException error) {
                break;
            }
        }

        DeleteSubnetRequest request = DeleteSubnetRequest.builder()
                .subnetId(getSubnetId())
                .build();

        client.deleteSubnet(request);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        String subnetId = getSubnetId();

        if (subnetId != null) {
            sb.append(subnetId);

        } else {
            sb.append("subnet");
        }

        String cidrBlock = getCidrBlock();

        if (cidrBlock != null) {
            sb.append(' ');
            sb.append(getCidrBlock());
        }

        String availabilityZone = getAvailabilityZone();

        if (availabilityZone != null) {
            sb.append(" in ");
            sb.append(availabilityZone);
        }

        return sb.toString();
    }

}
