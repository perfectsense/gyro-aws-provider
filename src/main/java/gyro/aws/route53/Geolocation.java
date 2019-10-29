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

package gyro.aws.route53;

import com.psddev.dari.util.StringUtils;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;

import java.util.Collections;
import java.util.List;

public class Geolocation extends Diffable {

    private String continentCode;
    private String countryCode;
    private String subdivisionCode;

    /**
     * The continent code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getContinentCode() {
        return continentCode != null ? continentCode.toUpperCase() : null;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    /**
     * The country code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getCountryCode() {
        return countryCode != null ? countryCode.toUpperCase() : null;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * The sub division code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getSubdivisionCode() {
        return subdivisionCode != null ? subdivisionCode.toUpperCase() : null;
    }

    public void setSubdivisionCode(String subdivisionCode) {
        this.subdivisionCode = subdivisionCode;
    }

    @Override
    public String primaryKey() {
        return "geolocation configuration";
    }

    @Override
    public List<ValidationError> validate() {
        if (StringUtils.isBlank(getContinentCode()) && StringUtils.isBlank(getCountryCode()) && StringUtils.isBlank(getSubdivisionCode())) {
            return Collections.singletonList(new ValidationError(this, null, "At least one of the param 'continent-code', 'country-code' or 'subdivision-code'"
                + " is required."));
        }

        return Collections.emptyList();
    }

}
