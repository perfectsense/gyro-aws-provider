/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.s3.model.IntelligentTieringStatus;
import software.amazon.awssdk.services.s3.model.Tiering;

public class IntelligentTieringConfiguration
    extends Diffable implements Copyable<software.amazon.awssdk.services.s3.model.IntelligentTieringConfiguration> {

    private String id;
    private IntelligentTieringFilter filter;
    private List<IntelligentTiering> tiering;
    private IntelligentTieringStatus status;

    /**
     * The name for the tiering.
     */
    @Required
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Configure intelligent tiering filter.
     *
     * @subresource gyro.aws.s3.IntelligentTieringFilter
     */
    @Updatable
    public IntelligentTieringFilter getFilter() {
        return filter;
    }

    public void setFilter(IntelligentTieringFilter filter) {
        this.filter = filter;
    }

    /**
     * Configure intelligent tiering.
     *
     * @subresource gyro.aws.s3.IntelligentTiering
     */
    @Updatable
    public List<IntelligentTiering> getTiering() {
        if (tiering == null) {
            tiering = new ArrayList<>();
        }

        return tiering;
    }

    public void setTiering(List<IntelligentTiering> tiering) {
        this.tiering = tiering;
    }

    /**
     * Status of the tiering.
     */
    @Updatable
    @ValidStrings({"Enabled", "Disabled"})
    public IntelligentTieringStatus getStatus() {
        return status;
    }

    public void setStatus(IntelligentTieringStatus status) {
        this.status = status;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.s3.model.IntelligentTieringConfiguration model) {
        setId(model.id());
        setStatus(model.status());

        setFilter(null);
        if (model.filter() != null) {
            IntelligentTieringFilter filter = newSubresource(IntelligentTieringFilter.class);
            filter.copyFrom(model.filter());
            setFilter(filter);
        }

        setTiering(null);
        if (model.hasTierings()) {
            setTiering(model.tierings().stream().map( tier -> {
                IntelligentTiering tiering = newSubresource(IntelligentTiering.class);
                tiering.copyFrom(tier);
                return tiering;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return getId();
    }

    protected software.amazon.awssdk.services.s3.model.IntelligentTieringConfiguration toIntelligentTieringConfiguration() {
        return software.amazon.awssdk.services.s3.model.IntelligentTieringConfiguration.builder()
            .id(getId())
            .filter(getFilter() != null ? getFilter().toIntelligentTieringFilter() : null)
            .tierings(getTiering().stream().map(IntelligentTiering::toTiering).toArray(Tiering[]::new))
            .status(getStatus())
            .build();
    }
}
