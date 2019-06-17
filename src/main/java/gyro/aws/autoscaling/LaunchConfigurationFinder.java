package gyro.aws.autoscaling;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsResponse;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query launch configuration.
 *
 * .. code-block:: gyro
 *
 *    launch-configuration: $(aws::launch-configuration EXTERNAL/* | launch-configuration-name = '')
 */
@Type("launch-configuration")
public class LaunchConfigurationFinder extends AwsFinder<AutoScalingClient, LaunchConfiguration, LaunchConfigurationResource> {
    private String launchConfigurationName;

    /**
     * The Launch Configuration Name.
     */
    public String getLaunchConfigurationName() {
        return launchConfigurationName;
    }

    public void setLaunchConfigurationName(String launchConfigurationName) {
        this.launchConfigurationName = launchConfigurationName;
    }

    @Override
    protected List<LaunchConfiguration> findAllAws(AutoScalingClient client) {
        List<LaunchConfiguration> launchConfigurations = new ArrayList<>();

        String marker = null;
        DescribeLaunchConfigurationsResponse response;
        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeLaunchConfigurations();
            } else {
                response = client.describeLaunchConfigurations(DescribeLaunchConfigurationsRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            launchConfigurations.addAll(response.launchConfigurations());

        } while (!ObjectUtils.isBlank(marker));

        return launchConfigurations;
    }

    @Override
    protected List<LaunchConfiguration> findAws(AutoScalingClient client, Map<String, String> filters) {
        List<LaunchConfiguration> launchConfigurations = new ArrayList<>();
        String marker = null;
        DescribeLaunchConfigurationsResponse response;

        if (filters.containsKey("launch-configuration-name") && !ObjectUtils.isBlank(filters.get("launch-configuration-name"))) {
            try {
                do {
                    if (ObjectUtils.isBlank(marker)) {
                        response = client.describeLaunchConfigurations(
                            DescribeLaunchConfigurationsRequest.builder()
                                .launchConfigurationNames(Collections.singleton(filters.get("launch-configuration-name")))
                                .build()
                        );
                    } else {
                        response = client.describeLaunchConfigurations(
                            DescribeLaunchConfigurationsRequest.builder()
                                .launchConfigurationNames(Collections.singleton(filters.get("launch-configuration-name")))
                                .nextToken(marker)
                                .build()
                        );
                    }

                    marker = response.nextToken();
                    launchConfigurations.addAll(response.launchConfigurations());

                } while (!ObjectUtils.isBlank(marker));
            } catch (AutoScalingException ex) {
                if (!ex.getLocalizedMessage().contains("does not exist")) {
                    throw ex;
                }
            }
        }

        return launchConfigurations;
    }
}
