package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.ReplicationConfiguration;
import software.amazon.awssdk.services.s3.model.ReplicationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S3ReplicationConfiguration extends Diffable implements Copyable<ReplicationConfiguration> {
    private RoleResource role;
    private List<S3ReplicationRule> replicationRule;

    /**
     * The ARN for an IAM Role that the s3 bucket assumes when replicating objects. (Required)
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }


    /**
     * Configure cross region replication rules. (Required)
     *
     * @subresource gyro.aws.s3.ReplicationRule
     */
    @Updatable
    public List<S3ReplicationRule> getReplicationRule() {
        if(replicationRule == null) {
            replicationRule = new ArrayList<>();
        }
        return replicationRule;
    }

    public void setReplicationRule(List<S3ReplicationRule> replicationRule) {
        this.replicationRule = replicationRule;
    }

    @Override
    public String primaryKey() {
        return "replication configuration";
    }

    @Override
    public void copyFrom(ReplicationConfiguration replicationConfiguration) {
        setRole(findById(RoleResource.class, replicationConfiguration.role()));

        getReplicationRule().clear();
        for (ReplicationRule replicationRule : replicationConfiguration.rules()) {
            S3ReplicationRule s3ReplicationRule = newSubresource(S3ReplicationRule.class);
            s3ReplicationRule.copyFrom(replicationRule);
            getReplicationRule().add(s3ReplicationRule);
        }
    }

    ReplicationConfiguration toReplicationConfiguration() {
        return ReplicationConfiguration.builder()
                .role(getRole().getArn())
                .rules(getReplicationRule().stream().map(S3ReplicationRule::toReplicationRule)
                        .collect(Collectors.toList()))
                .build();
    }
}
