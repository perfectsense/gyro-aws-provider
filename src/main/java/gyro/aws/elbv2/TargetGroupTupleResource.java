package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

public class TargetGroupTupleResource extends Diffable implements Copyable<TargetGroupTuple> {

    private TargetGroupResource targetGroup;
    private Integer weight;

    /**
     * The Target group to which requests are forwarded
     */
    @Updatable
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     * The weight of the ratio of requests forwarded to the given target group.
     * The ratio is this weight / total weights from all Target groups configured
     */
    @Updatable
    public Integer getWeight() {
        return weight == null ?
            weight = 1 :
            weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getTargetGroup().getArn(), getWeight());
    }

    public String getTargetGroupArn() {
        return getTargetGroup() != null ?
            getTargetGroup().getArn() :
            null;
    }

    @Override
    public void copyFrom(TargetGroupTuple targetGroupTuple) {
        setTargetGroup(targetGroupTuple.targetGroupArn() != null ? findById(TargetGroupResource.class, targetGroupTuple.targetGroupArn()) : null);
        setWeight(targetGroupTuple.weight());
    }

    public TargetGroupTuple toTargetGroupTuple() {
        return TargetGroupTuple.builder()
            .targetGroupArn(getTargetGroup().getArn())
            .weight(getWeight())
            .build();
    }
}
