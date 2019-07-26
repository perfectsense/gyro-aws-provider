package gyro.aws.autoscaling;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query launch configuration.
 *
 * .. code-block:: gyro
 *
 *    launch-configuration: $(aws::launch-configuration EXTERNAL/* | name = '')
 */
@Type("launch-configuration")
public class LaunchConfigurationFinder extends AwsFinder<AutoScalingClient, LaunchConfiguration, LaunchConfigurationResource> {
    private String name;

    /**
     * The Launch Configuration Name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<LaunchConfiguration> findAllAws(AutoScalingClient client) {
        return client.describeLaunchConfigurationsPaginator().launchConfigurations().stream().collect(Collectors.toList());
    }

    @Override
    protected List<LaunchConfiguration> findAws(AutoScalingClient client, Map<String, String> filters) {
        List<LaunchConfiguration> launchConfigurations = new ArrayList<>();

        if (filters.containsKey("name") && !ObjectUtils.isBlank(filters.get("name"))) {
            try {
                launchConfigurations.addAll(client.describeLaunchConfigurations(
                    DescribeLaunchConfigurationsRequest.builder()
                        .launchConfigurationNames(Collections.singleton(filters.get("name")))
                        .build()).launchConfigurations());
            } catch (AutoScalingException ex) {
                if (!ex.getLocalizedMessage().contains("does not exist")) {
                    throw ex;
                }
            }
        }

        return launchConfigurations;
    }
}
