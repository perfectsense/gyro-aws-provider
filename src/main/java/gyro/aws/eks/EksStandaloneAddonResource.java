package gyro.aws.eks;

import gyro.core.Type;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.DiffableType;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.DescribeAddonResponse;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;

@Type("eks-addon")
public class EksStandaloneAddonResource extends EksAddonResource {

    private EksClusterResource cluster;

    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        try {
            DescribeAddonResponse response = client.describeAddon(r -> r
                .clusterName(clusterName())
                .addonName(getAddonName()));

            copyFrom(response.addon());
        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public String primaryKey() {
        String name = DiffableInternals.getName(this);
        return String.format("%s::%s", DiffableType.getInstance(getClass()).getName(), name);
    }

    @Override
    protected String clusterName() {
        return cluster.getName();
    }
}
