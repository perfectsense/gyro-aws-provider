/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import software.amazon.awssdk.services.autoscaling.model.LaunchConfiguration;

/**
 * Query launch configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    launch-configuration: $(external-query aws::launch-configuration { name: 'frontend' })
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
        return client.describeLaunchConfigurationsPaginator().launchConfigurations()
            .stream().collect(Collectors.toList());
    }

    @Override
    protected List<LaunchConfiguration> findAws(AutoScalingClient client, Map<String, String> filters) {
        List<LaunchConfiguration> launchConfigurations = new ArrayList<>();

        if (filters.containsKey("name") && !ObjectUtils.isBlank(filters.get("name"))) {
            try {
                launchConfigurations.addAll(client.describeLaunchConfigurations(
                    DescribeLaunchConfigurationsRequest.builder().launchConfigurationNames(
                        Collections.singleton(filters.get("name"))).build()).launchConfigurations());
            } catch (AutoScalingException ex) {
                if (!ex.getLocalizedMessage().contains("does not exist")) {
                    throw ex;
                }
            }
        }

        return launchConfigurations;
    }
}
