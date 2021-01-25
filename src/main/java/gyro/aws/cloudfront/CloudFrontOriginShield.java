package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.cloudfront.model.OriginShield;

public class CloudFrontOriginShield extends Diffable implements Copyable<OriginShield> {

    private Boolean enabled;
    private String region;

    @Required
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Updatable
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void copyFrom(OriginShield model) {
        setEnabled(model.enabled());
        setRegion(model.originShieldRegion());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected OriginShield toOriginShield() {
        OriginShield.Builder builder = OriginShield.builder().enabled(getEnabled());

        if (getEnabled()) {
            builder = builder.originShieldRegion(getRegion());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getEnabled() && getRegion() != null) {
            errors.add(new ValidationError(this, null, "'region' cannot be specified if 'enabled' is set to 'false'."));
        }

        return errors;
    }
}
