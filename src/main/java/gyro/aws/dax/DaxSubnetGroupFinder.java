package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.SubnetGroup;

public class DaxSubnetGroupFinder extends AwsFinder<DaxClient, SubnetGroup, DaxSubnetGroupResource> {

    private List<String> names;

    public List<String> getNames() {
        if (names == null) {
            names = new ArrayList<>();
        }

        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    protected List<SubnetGroup> findAllAws(DaxClient client) {
        return client.describeSubnetGroups().subnetGroups();
    }

    @Override
    protected List<SubnetGroup> findAws(
        DaxClient client, Map<String, String> filters) {
        return client.describeSubnetGroups(r -> r.subnetGroupNames(filters.get("names"))).subnetGroups();
    }
}
