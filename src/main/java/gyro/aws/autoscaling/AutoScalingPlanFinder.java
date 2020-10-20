package gyro.aws.autoscaling;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;

@Type("autoscaling-plan")
public class AutoScalingPlanFinder extends AwsFinder<AutoScalingPlansClient, ScalingPlan, AutoScalingPlanResource> {

    private List<String> scalingPlanNames;

    public List<String> getScalingPlanNames() {
        return scalingPlanNames;
    }

    public void setScalingPlanNames(List<String> scalingPlanNames) {
        this.scalingPlanNames = scalingPlanNames;
    }

    @Override
    protected List<ScalingPlan> findAllAws(AutoScalingPlansClient client) {
        return client.describeScalingPlans().scalingPlans();
    }

    @Override
    protected List<ScalingPlan> findAws(
        AutoScalingPlansClient client, Map<String, String> filters) {
        return client.describeScalingPlans(r -> r.scalingPlanNames(filters.get("scaling-plan-names"))).scalingPlans();
    }
}
