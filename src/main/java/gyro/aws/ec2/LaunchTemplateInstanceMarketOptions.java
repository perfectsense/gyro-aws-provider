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
