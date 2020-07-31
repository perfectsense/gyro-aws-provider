/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.GeoMatchStatement;

public class GeoMatchStatementResource extends Diffable implements Copyable<GeoMatchStatement> {

    private Set<String> countryCodes;

    /**
     * A set of 2 character country codes based on ISO 3166 on which to filter the request. (Required)
     */
    @Required
    public Set<String> getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(Set<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    @Override
    public void copyFrom(GeoMatchStatement geoMatchStatement) {
        setCountryCodes(new HashSet<>(geoMatchStatement.countryCodesAsStrings()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    GeoMatchStatement toGeoMatchStatement() {
        return GeoMatchStatement.builder()
            .countryCodesWithStrings(getCountryCodes())
            .build();
    }
}
