package gyro.aws.ec2;

import java.util.Date;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidNumbers;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.InstanceInterruptionBehavior;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateSpotMarketOptionsRequest;
import software.amazon.awssdk.services.ec2.model.SpotInstanceType;

public class LaunchTemplateSpotMarketOptions
    extends Diffable implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateSpotMarketOptions> {

    private Integer blockDurationMinutes;
    private InstanceInterruptionBehavior instanceInterruptionBehavior;
    private String maxPrice;
    private SpotInstanceType spotInstanceType;
    private Date validUntil;

    /**
     * The required duration for the Spot Instances in minutes.
     */
    @Updatable
    @ValidNumbers({ 60, 120, 180, 240, 300, 360 })
    public Integer getBlockDurationMinutes() {
        return blockDurationMinutes;
    }

    public void setBlockDurationMinutes(Integer blockDurationMinutes) {
        this.blockDurationMinutes = blockDurationMinutes;
    }

    /**
     * The behavior when a Spot Instance is interrupted.
     */
    @Updatable
    @ValidStrings({ "HIBERNATE", "STOP", "TERMINATE" })
    public InstanceInterruptionBehavior getInstanceInterruptionBehavior() {
        return instanceInterruptionBehavior;
    }

    public void setInstanceInterruptionBehavior(InstanceInterruptionBehavior instanceInterruptionBehavior) {
        this.instanceInterruptionBehavior = instanceInterruptionBehavior;
    }

    /**
     * The maximum hourly price you're willing to pay for the Spot Instances.
     */
    @Updatable
    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    /**
     * The Spot Instance request type.
     */
    @Updatable
    @ValidStrings({ "ONE_TIME", "PERSISTENT" })
    public SpotInstanceType getSpotInstanceType() {
        return spotInstanceType;
    }

    public void setSpotInstanceType(SpotInstanceType spotInstanceType) {
        this.spotInstanceType = spotInstanceType;
    }

    /**
     * The end date of the request.
     */
    @Updatable
    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateSpotMarketOptions model) {
        setBlockDurationMinutes(model.blockDurationMinutes());
        setInstanceInterruptionBehavior(model.instanceInterruptionBehavior());
        setMaxPrice(model.maxPrice());
        setValidUntil(model.validUntil() != null ? Date.from(model.validUntil()) : null);
        setSpotInstanceType(model.spotInstanceType());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateSpotMarketOptionsRequest toLaunchTemplateSpotMarketOptionsRequest() {
        return LaunchTemplateSpotMarketOptionsRequest.builder()
            .blockDurationMinutes(getBlockDurationMinutes())
            .instanceInterruptionBehavior(getInstanceInterruptionBehavior())
            .maxPrice(getMaxPrice())
            .validUntil(getValidUntil().toInstant())
            .spotInstanceType(getSpotInstanceType())
            .build();
    }
}
