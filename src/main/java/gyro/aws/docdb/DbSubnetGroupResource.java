package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbSubnetGroupResponse;
import software.amazon.awssdk.services.docdb.model.DBSubnetGroup;
import software.amazon.awssdk.services.docdb.model.DescribeDbSubnetGroupsResponse;
import software.amazon.awssdk.services.docdb.model.Subnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName("db-subnet-group")
public class DbSubnetGroupResource extends DocDbTaggableResource {
    private String dbSubnetGroupDescription;
    private String dbSubnetGroupName;
    private List<String> subnetIds;

    private String arn;

    @ResourceDiffProperty(updatable = true)
    public String getDbSubnetGroupDescription() {
        return dbSubnetGroupDescription;
    }

    public void setDbSubnetGroupDescription(String dbSubnetGroupDescription) {
        this.dbSubnetGroupDescription = dbSubnetGroupDescription;
    }

    public String getDbSubnetGroupName() {
        return dbSubnetGroupName;
    }

    public void setDbSubnetGroupName(String dbSubnetGroupName) {
        this.dbSubnetGroupName = dbSubnetGroupName;
    }

    @ResourceDiffProperty(updatable = true)
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        } else {
            Collections.sort(subnetIds);
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected String getId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DescribeDbSubnetGroupsResponse response = client.describeDBSubnetGroups(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
        );

        if (!response.dbSubnetGroups().isEmpty()) {
            DBSubnetGroup dbSubnetGroup = response.dbSubnetGroups().get(0);

            setDbSubnetGroupDescription(dbSubnetGroup.dbSubnetGroupDescription());
            setArn(dbSubnetGroup.dbSubnetGroupArn());
            setSubnetIds(dbSubnetGroup.subnets().stream().map(Subnet::subnetIdentifier).collect(Collectors.toList()));

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbSubnetGroupResponse response = client.createDBSubnetGroup(
            r -> r.dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .dbSubnetGroupName(getDbSubnetGroupName())
                .subnetIds(getSubnetIds())
        );

        setArn(response.dbSubnetGroup().dbSubnetGroupArn());
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        client.modifyDBSubnetGroup(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
                .dbSubnetGroupDescription(getDbSubnetGroupDescription())
                .subnetIds(getSubnetIds())
        );
    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBSubnetGroup(
            r -> r.dbSubnetGroupName(getDbSubnetGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db subnet group");

        if (!ObjectUtils.isBlank(getDbSubnetGroupName())) {
            sb.append(" - ").append(getDbSubnetGroupName());
        }

        return sb.toString();
    }
}
