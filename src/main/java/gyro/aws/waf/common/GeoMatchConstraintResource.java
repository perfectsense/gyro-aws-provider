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

package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateGeoMatchSetRequest;

import java.util.Set;

public abstract class GeoMatchConstraintResource extends AbstractWafResource implements Copyable<GeoMatchConstraint> {
    private String value;
    private String type;

    /**
     * The value filter. Uses two letter country codes (i.e. US) when type selected as ``COUNTRY``. (Required)
     */
    @Required
    public String getValue() {
        return value != null ? value.toUpperCase() : null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of geo match filter. Allowed values are ``Country``. (Required)
     */
    @Required
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(GeoMatchConstraint geoMatchConstraint) {
        setType(geoMatchConstraint.typeAsString());
        setValue(geoMatchConstraint.valueAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        saveGeoMatchConstraint(toGeoMatchConstraint(), false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        saveGeoMatchConstraint(toGeoMatchConstraint(), true);
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getValue(), getType());
    }

    protected abstract void saveGeoMatchConstraint(GeoMatchConstraint geoMatchConstraint, boolean isDelete);

    private GeoMatchConstraint toGeoMatchConstraint() {
        return GeoMatchConstraint.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    protected UpdateGeoMatchSetRequest.Builder toUpdateGeoMatchSetRequest(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        GeoMatchSetResource parent = (GeoMatchSetResource) parent();

        GeoMatchSetUpdate geoMatchSetUpdate = GeoMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .geoMatchConstraint(geoMatchConstraint)
            .build();

        return UpdateGeoMatchSetRequest.builder()
            .geoMatchSetId(parent.getId())
            .updates(geoMatchSetUpdate);
    }
}
