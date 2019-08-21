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
import software.amazon.awssdk.services.ec2.model.CreateRouteTableResponse;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.RouteTableAssociation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a VPC route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *     aws::route-table route-table-example
 *         vpc: $(aws::vpc vpc-example)
 *
 *         tags:
 *             Name: route-table-example
 *         end
 *     end
 */
@Type("route-table")
public class RouteTableResource extends Ec2TaggableResource<RouteTable> implements Copyable<RouteTable> {

    private VpcResource vpc;
    private Set<SubnetResource> subnets;
    private String id;
    private String ownerId;

    /**
     * The VPC to create a Route Table for. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * Subnets to associate with this Route Table.
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The ID of the Route Table.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The owner ID of the Route Table.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(RouteTable routeTable) {
        setId(routeTable.routeTableId());
        setVpc(!ObjectUtils.isBlank(routeTable.vpcId()) ? findById(VpcResource.class, routeTable.vpcId()) : null);
        setOwnerId(routeTable.ownerId());

        getSubnets().clear();
        for (RouteTableAssociation rta : routeTable.associations()) {
            if (!rta.main()) {
                getSubnets().add(!ObjectUtils.isBlank(rta.subnetId()) ? findById(SubnetResource.class, rta.subnetId()) : null);
            }
        }
    }

    @Override
    public boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private RouteTable getRouteTable(Ec2Client client) {
        RouteTable routeTable = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load route table.");
        }

        try {
            DescribeRouteTablesResponse response = client.describeRouteTables(r -> r.filters(
                Filter.builder().name("route-table-id").values(getId()).build()
            ));

            if (!response.routeTables().isEmpty()) {
                routeTable = response.routeTables().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return routeTable;
    }
}
