package gyro.aws.dax;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.ParameterGroup;

public class DaxParameterGroupFinder extends AwsFinder<DaxClient, ParameterGroup, DaxParameterGroupResource> {

    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    protected List<ParameterGroup> findAllAws(DaxClient client) {
        return client.describeParameterGroups().parameterGroups();
    }

    @Override
    protected List<ParameterGroup> findAws(
        DaxClient client, Map<String, String> filters) {
        return client.describeParameterGroups(r -> r.parameterGroupNames(filters.get("names"))).parameterGroups();
    }
}
