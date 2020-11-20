package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeSubnetGroupsResponse;
import software.amazon.awssdk.services.dax.model.SubnetGroup;

public class DaxSubnetGroupResource extends AwsResource implements Copyable<SubnetGroup> {

    private String description;
    private String name;
    private List<DaxSubnet> subnets;
    private String vpcId;

    /**
     * The description of the subnet group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the subnet group.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of subnets of the subnet group.
     */
    @Output
    public List<DaxSubnet> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    @Required
    public void setSubnets(List<DaxSubnet> subnets) {
        this.subnets = subnets;
    }

    /**
     * The VPC ID of the subnet group.
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public void copyFrom(SubnetGroup model) {
        setDescription(model.description());
        setName(model.subnetGroupName());
        setVpcId(model.vpcId());

        getSubnets().clear();
        if (model.subnets() != null) {
            model.subnets().forEach(subnet -> {
                DaxSubnet daxSubnet = newSubresource(DaxSubnet.class);
                daxSubnet.copyFrom(subnet);
                getSubnets().add(daxSubnet);
            });
        }
    }

    @Override
    public boolean refresh() {
        DaxClient client = createClient(DaxClient.class);

        DescribeSubnetGroupsResponse response = client.describeSubnetGroups(r -> r.subnetGroupNames(getName()));

        if (response == null || response.subnetGroups().isEmpty()) {
            return false;
        }

        copyFrom(response.subnetGroups().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.createSubnetGroup(r -> r
            .subnetGroupName(getName())
            .subnetIds(getSubnets().stream().map(DaxSubnet::getIdentifier).collect(Collectors.toList()))
            .description(getDescription())
            .build()
        );

        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.updateSubnetGroup(r -> r
            .subnetGroupName(getName())
            .subnetIds(getSubnets().stream().map(DaxSubnet::getIdentifier).collect(Collectors.toList()))
            .description(getDescription())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteSubnetGroup(r -> r.subnetGroupName(getName()));
    }
}
