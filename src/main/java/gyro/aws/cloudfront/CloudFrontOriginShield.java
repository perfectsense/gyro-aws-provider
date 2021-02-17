/*
 * Copyright 2021, Brightspot.
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

    /**
     * When set to ``true`, CloudFront routes all requests through Origin Shield.
     */
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

    /**
     * The region to check the latency with.
     */
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
