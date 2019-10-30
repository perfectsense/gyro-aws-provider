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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.GeoRestriction;
import software.amazon.awssdk.services.cloudfront.model.Restrictions;

import java.util.HashSet;
import java.util.Set;

public class CloudFrontGeoRestriction extends Diffable implements Copyable<GeoRestriction> {

    private String type;
    private Set<String> restrictions;

    /**
     * Type of restriction. Valid values are ``Whitelist`` or ``Blacklist``.
     */
    @Updatable
    public String getType() {
        if (type == null) {
            type = "none";
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * List of countries to whitelist or blacklist. Uses two letter country codes (i.e. US).
     */
    @Updatable
    public Set<String> getRestrictions() {
        if (restrictions == null) {
            restrictions = new HashSet<>();
        }

        return restrictions;
    }

    public void setRestrictions(Set<String> restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void copyFrom(GeoRestriction geoRestriction) {
        setType(geoRestriction.restrictionTypeAsString());
        setRestrictions(new HashSet<>(geoRestriction.items()));
    }

    @Override
    public String primaryKey() {
        return "geo-restriction";
    }

    Restrictions toRestrictions() {
        return Restrictions.builder()
            .geoRestriction(r -> r.restrictionType(getType())
                .items(getRestrictions())
                .quantity(getRestrictions().size())).build();
    }
}
