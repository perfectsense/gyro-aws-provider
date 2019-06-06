package gyro.aws.autoscaling;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query auto scaling group.
 *
 * .. code-block:: gyro
 *
 *    auto-scaling-group: $(aws::auto-scaling-group EXTERNAL/* | auto-scaling-group-name = '')
 */
@Type("auto-scaling-group")
public class AutoScalingGroupFinder extends AwsFinder<AutoScalingClient, AutoScalingGroup, AutoScalingGroupResource> {
    private String autoScalingGroupName;

    /**
     * The name of the auto scaling group.
     */
    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    @Override
    protected List<AutoScalingGroup> findAllAws(AutoScalingClient client) {
        return client.describeAutoScalingGroups().autoScalingGroups();
    }

    @Override
    protected List<AutoScalingGroup> findAws(AutoScalingClient client, Map<String, String> filters) {
        List<AutoScalingGroup> autoScalingGroups = new ArrayList<>();

        if (filters.containsKey("auto-scaling-group-name") && !ObjectUtils.isBlank(filters.get("auto-scaling-group-name"))) {

            try {
                autoScalingGroups.addAll(
                    client.describeAutoScalingGroups(
                        r -> r.autoScalingGroupNames(Collections.singleton(filters.get("auto-scaling-group-name")))
                    ).autoScalingGroups()
                );
            } catch (AutoScalingException ex) {
                if (!ex.getLocalizedMessage().contains("does not exist")) {
                    throw ex;
                }
            }
        }

        return autoScalingGroups;
    }
}
