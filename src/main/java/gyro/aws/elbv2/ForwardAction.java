package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ForwardActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupStickinessConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

public class ForwardAction extends Diffable implements Copyable<ForwardActionConfig> {

    private List<TargetGroupTupleResource> targetGroupWeight;
    private TargetGroupStickiness targetGroupStickiness;

    @Updatable
    public List<TargetGroupTupleResource> getTargetGroupWeight() {
        return targetGroupWeight == null ?
            targetGroupWeight = new ArrayList<>() :
            targetGroupWeight;
    }

    public void setTargetGroupWeight(List<TargetGroupTupleResource> targetGroupWeight) {
        this.targetGroupWeight = targetGroupWeight;
    }

    @Updatable
    public TargetGroupStickiness getTargetGroupStickiness() {
        return targetGroupStickiness;
    }

    public void setTargetGroupStickiness(TargetGroupStickiness targetGroupStickiness) {
        this.targetGroupStickiness = targetGroupStickiness;
    }

    @Override
    public void copyFrom(ForwardActionConfig forwardActionConfig) {

        getTargetGroupWeight().clear();
        if (forwardActionConfig.hasTargetGroups()) {
            for (TargetGroupTuple targetGroupTuple : forwardActionConfig.targetGroups()) {
                TargetGroupTupleResource targetGroupWeight = newSubresource(TargetGroupTupleResource.class);
                targetGroupWeight.copyFrom(targetGroupTuple);
                getTargetGroupWeight().add(targetGroupWeight);
            }
        }

        TargetGroupStickinessConfig targetGroupStickinessConfig = forwardActionConfig.targetGroupStickinessConfig();
        if (targetGroupStickinessConfig != null) {
            TargetGroupStickiness targetGroupStickiness = newSubresource(TargetGroupStickiness.class);
            targetGroupStickiness.copyFrom(targetGroupStickinessConfig);
        }
    }

    public List<TargetGroupTuple> toTargetGroupTuples() {
        return getTargetGroupWeight().stream()
            .map(TargetGroupTupleResource::toTargetGroupTuple)
            .collect(Collectors.toList());
    }

    public TargetGroupStickinessConfig toTargetGroupStickinessConfig() {
        if (getTargetGroupStickiness() != null) {
            return TargetGroupStickinessConfig.builder()
                .durationSeconds(getTargetGroupStickiness().getDuration())
                .enabled(getTargetGroupStickiness().getEnabled())
                .build();
        }

        return null;
    }

    public ForwardActionConfig toForwardActionConfig() {
        return ForwardActionConfig.builder()
            .targetGroups(toTargetGroupTuples())
            .targetGroupStickinessConfig(toTargetGroupStickinessConfig())
            .build();
    }

    @Override
    public String primaryKey() {
        return getTargetGroup().stream()
            .map(TargetGroupTupleResource::getTargetGroupArn)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("/"));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        return errors;
    }
}
