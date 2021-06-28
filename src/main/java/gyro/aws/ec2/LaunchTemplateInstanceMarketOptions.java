/*
 * Copyright 2021, Perfect Sense.
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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMarketOptionsRequest;
import software.amazon.awssdk.services.ec2.model.MarketType;

public class LaunchTemplateInstanceMarketOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMarketOptions> {

    private MarketType marketType;
    private LaunchTemplateSpotMarketOptions spotMarketOptions;

    /**
     * The market type.
     */
    @ValidStrings({ "SPOT" })
    @Updatable
    public MarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(MarketType marketType) {
        this.marketType = marketType;
    }

    /**
     * The options for Spot Instances.
     */
    @Updatable
    public LaunchTemplateSpotMarketOptions getSpotMarketOptions() {
        return spotMarketOptions;
    }

    public void setSpotMarketOptions(LaunchTemplateSpotMarketOptions spotMarketOptions) {
        this.spotMarketOptions = spotMarketOptions;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMarketOptions model) {
        setMarketType(model.marketType());

        setSpotMarketOptions(null);
        if (model.spotOptions() != null) {
            LaunchTemplateSpotMarketOptions options = newSubresource(LaunchTemplateSpotMarketOptions.class);
            options.copyFrom(model.spotOptions());
            setSpotMarketOptions(options);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateInstanceMarketOptionsRequest toLaunchTemplateInstanceMarketOptionsRequest() {
        return LaunchTemplateInstanceMarketOptionsRequest.builder()
            .marketType(getMarketType())
            .spotOptions(getSpotMarketOptions() == null ? null : getSpotMarketOptions()
                .toLaunchTemplateSpotMarketOptionsRequest())
            .build();
    }
}
