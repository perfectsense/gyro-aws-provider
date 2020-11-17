package gyro.aws.dax;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeParameterGroupsResponse;
import software.amazon.awssdk.services.dax.model.ParameterGroup;

public class DaxParameterGroupResource extends AwsResource implements Copyable<ParameterGroup> {

    private String description;
    private String name;
    private List<DaxParameterNameValue> parameterNameValues;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DaxParameterNameValue> getParameterNameValues() {
        return parameterNameValues;
    }

    public void setParameterNameValues(List<DaxParameterNameValue> parameterNameValues) {
        this.parameterNameValues = parameterNameValues;
    }

    @Override
    public void copyFrom(ParameterGroup model) {
        setDescription(model.description());
        setName(model.parameterGroupName());
    }

    @Override
    public boolean refresh() {
        DaxClient client = createClient(DaxClient.class);
        DescribeParameterGroupsResponse response;

        response = client.describeParameterGroups(r -> r.parameterGroupNames(getName()));

        if (response == null || response.parameterGroups().isEmpty()) {
            return false;
        }

        copyFrom(response.parameterGroups().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.createParameterGroup(r -> r
            .description(getDescription())
            .parameterGroupName(getName()));

        refresh();

    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.updateParameterGroup(r -> r
            .parameterGroupName(getName())
            .parameterNameValues(getParameterNameValues().stream()
                .map(DaxParameterNameValue::toParameterNameValues)
                .collect(
                    Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteParameterGroup(r -> r.parameterGroupName(getName()));
    }
}
