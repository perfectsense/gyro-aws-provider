package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.OptionGroup;
import software.amazon.awssdk.services.rds.model.OptionGroupNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("db-option-group")
public class DbOptionGroupFinder extends AwsFinder<RdsClient, OptionGroup, DbOptionGroupResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<OptionGroup> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeOptionGroups(r -> r.optionGroupName(filters.get("name"))).optionGroupsList();
        } catch (OptionGroupNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<OptionGroup> findAllAws(RdsClient client) {
        return client.describeOptionGroups().optionGroupsList();
    }

}
