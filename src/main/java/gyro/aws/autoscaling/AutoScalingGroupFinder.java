package gyro.aws.autoscaling;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query auto scaling group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    autoscaling-group: $(external-query aws::autoscaling-group { name: 'frontend' })
 */
@Type("autoscaling-group")
public class AutoScalingGroupFinder extends AwsFinder<AutoScalingClient, AutoScalingGroup, AutoScalingGroupResource> {
    private String name;

    /**
     * The name of the Auto Scaling Group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<AutoScalingGroup> findAllAws(AutoScalingClient client) {
        return client.describeAutoScalingGroupsPaginator().autoScalingGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<AutoScalingGroup> findAws(AutoScalingClient client, Map<String, String> filters) {
        List<AutoScalingGroup> autoScalingGroups = new ArrayList<>();

        if (filters.containsKey("name") && !ObjectUtils.isBlank(filters.get("name"))) {
            try {
                autoScalingGroups.addAll(client.describeAutoScalingGroups(
                    DescribeAutoScalingGroupsRequest.builder()
                        .autoScalingGroupNames(Collections.singleton(filters.get("name")))
                        .build()).autoScalingGroups());

            } catch (AutoScalingException ex) {
                if (!ex.getLocalizedMessage().contains("does not exist")) {
                    throw ex;
                }
            }
        }

        return autoScalingGroups;
    }
}
