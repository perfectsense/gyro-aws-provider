package gyro.aws.ec2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

/**
 * Query security group rules.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    security-group-rules: $(external-query aws::security-group-rules { group-name: ''})
 */
@Type("security-group-rules")
public class SecurityGroupRulesFinder extends AwsFinder<Ec2Client, SecurityGroup, SecurityGroupRulesResource> {

    private String groupId;
    private String groupName;

    /**
     * The ID of the security group where the security group rules reside.
     */
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * The name of the security group where the security group rules reside.
     */
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    protected List<SecurityGroup> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSecurityGroupsPaginator(r -> r.filters(createFilters(filters))).securityGroups().stream().collect(
            Collectors.toList());
    }

    @Override
    protected List<SecurityGroup> findAllAws(Ec2Client client) {
        return client.describeSecurityGroupsPaginator().securityGroups().stream().collect(Collectors.toList());
    }
}
