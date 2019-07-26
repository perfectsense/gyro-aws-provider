package gyro.aws.s3;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.*;

public class S3ReplicationRule extends Diffable implements Copyable<ReplicationRule> {
    private S3Destination destination;
    private Integer priority;
    private String prefix;
    private String id;
    private ReplicationRuleStatus status;
    private DeleteMarkerReplicationStatus deleteMarkerReplicationStatus;
    private S3ReplicationRuleFilter filter;
    private S3SourceSelectionCriteria sourceSelectionCriteria;

    /**
     * The destination bucket and config for this rule. (Required)
     */
     @Updatable
    public S3Destination getDestination() {
        return destination;
    }

    /**
     * @return
     */
    public void setDestination(S3Destination destination) {
        this.destination = destination;
    }

    /**
     * Priority of this rule. Defaults to 1
     */
    @Updatable
    public Integer getPriority() {
        return priority;
    }


    public void setPriority(Integer priority) {
        this.priority = priority;
    }

     /**
     * Object prefix that this rule applies to.
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Unique name for this rule. (Required)
     */
    @Updatable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

     /**
     * The state of this replication rule. Valid valiues ``Enabled`` or ``Disabled``. (Required)
     */
    @Updatable
    public ReplicationRuleStatus getStatus() {
        return status;
    }

    public void setStatus(ReplicationRuleStatus status) {
        this.status = status;
    }

    /**
     * State of the delete marker replication. Valid values ``Enabled`` or ``Disabled``. (Required)
     */
    @Updatable
    public DeleteMarkerReplicationStatus getDeleteMarkerReplicationStatus() {
        return deleteMarkerReplicationStatus;
    }

    public void setDeleteMarkerReplicationStatus(DeleteMarkerReplicationStatus deleteMarkerReplicationStatus) {
        this.deleteMarkerReplicationStatus = deleteMarkerReplicationStatus;
    }
    /**
     * Rule for selecting a subset of objects to replicate based on tags or prefix or both.
     */
    @Updatable
    public S3ReplicationRuleFilter getFilter() {
        return filter;
    }

    public void setFilter(S3ReplicationRuleFilter filter) {
        this.filter = filter;
    }
    /**
     * Describes additional filters for the objects which should be replicated. Currently
     * only supports encrypted objects.
     *
     * @subresource gyro.aws.s3.S3SourceSelectionCriteria
     */
    @Updatable
    public S3SourceSelectionCriteria getSourceSelectionCriteria() {
        return sourceSelectionCriteria;
    }

    public void setSourceSelectionCriteria(S3SourceSelectionCriteria sourceSelectionCriteria) {
        this.sourceSelectionCriteria = sourceSelectionCriteria;
    }

    @Override
    public void copyFrom(ReplicationRule replicationRule) {
        setPriority(replicationRule.priority());
        setPrefix(replicationRule.prefix());
        setId(replicationRule.id());
        setStatus(replicationRule.status());
        setFilter(newSubresource(S3ReplicationRuleFilter.class));

        if(replicationRule.destination() != null){
            S3Destination destination = newSubresource(S3Destination.class);
            destination.copyFrom(replicationRule.destination());

            setDestination(destination);
        }

        if(replicationRule.deleteMarkerReplication() != null){
            setDeleteMarkerReplicationStatus(replicationRule.deleteMarkerReplication().status());
        }

        if(replicationRule.filter() != null){
            getFilter().copyFrom(replicationRule.filter());
        }

        if(replicationRule.sourceSelectionCriteria() != null){
            S3SourceSelectionCriteria criteria = newSubresource(S3SourceSelectionCriteria.class);
            criteria.copyFrom(replicationRule.sourceSelectionCriteria());
            setSourceSelectionCriteria(criteria);
        }
    }

    @Override
    public String toDisplayString() {
        return "replication rules " + getId();
    }

    @Override
    public String primaryKey(){
        return getId();
    }

    ReplicationRule toReplicationRule(){
        ReplicationRule.Builder builder = ReplicationRule.builder();

        if(getFilter() == null){
            setFilter(newSubresource(S3ReplicationRuleFilter.class));
        }

        builder.destination(getDestination().toDestination())
            .priority(getPriority())
            .prefix(getPrefix())
            .id(getId())
            .status(getStatus())
            .deleteMarkerReplication(
                m -> m.status(getDeleteMarkerReplicationStatus())
            )
            .filter(getFilter().toReplicationRuleFilter());

        if(getSourceSelectionCriteria() != null){
            builder.sourceSelectionCriteria(getSourceSelectionCriteria().toSourceSelectionCriteria());
        }

        return builder.build();
    }
}
