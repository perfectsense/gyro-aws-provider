package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.OptionGroup;
import software.amazon.awssdk.services.rds.model.OptionGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db option group.
 *
 * .. code-block:: gyro
 *
 *    option-groups: $(external-query aws::db-option-group { name: 'option-group-example'})
 */
@Type("db-option-group")
public class DbOptionGroupFinder extends AwsFinder<RdsClient, OptionGroup, DbOptionGroupResource> {

    private String name;

    /**
     * The name of the option group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<OptionGroup> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeOptionGroups(r -> r.optionGroupName(filters.get("name"))).optionGroupsList();
        } catch (OptionGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<OptionGroup> findAllAws(RdsClient client) {
        return client.describeOptionGroupsPaginator().optionGroupsList().stream().collect(Collectors.toList());
    }

}
