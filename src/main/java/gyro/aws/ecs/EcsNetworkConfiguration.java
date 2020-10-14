/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;

public class EcsNetworkConfiguration extends Diffable implements Copyable<NetworkConfiguration> {

    private EcsAwsVpcConfiguration awsVpcConfiguration;

    /**
     * The VPC configuration for the service. (Required)
     */
    @Required
    @Updatable
    public EcsAwsVpcConfiguration getAwsVpcConfiguration() {
        return awsVpcConfiguration;
    }

    public void setAwsVpcConfiguration(EcsAwsVpcConfiguration awsVpcConfiguration) {
        this.awsVpcConfiguration = awsVpcConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(NetworkConfiguration model) {
        EcsAwsVpcConfiguration config = newSubresource(EcsAwsVpcConfiguration.class);
        config.copyFrom(model.awsvpcConfiguration());
        setAwsVpcConfiguration(config);
    }

    public NetworkConfiguration toNetworkConfiguration() {
        return NetworkConfiguration.builder()
            .awsvpcConfiguration(getAwsVpcConfiguration().toAwsVpcConfiguration())
            .build();
    }
}
