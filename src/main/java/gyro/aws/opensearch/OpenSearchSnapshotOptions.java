/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.SnapshotOptions;

public class OpenSearchSnapshotOptions extends Diffable implements Copyable<SnapshotOptions> {

    private Integer automatedSnapshotStartHour;

    /**
     * The hour, in UTC format, when the service takes a daily automated snapshot of the specified OpenSearch domain.
     */
    @Required
    @Updatable
    @Range(min = 0, max = 23)
    public Integer getAutomatedSnapshotStartHour() {
        return automatedSnapshotStartHour;
    }

    public void setAutomatedSnapshotStartHour(Integer automatedSnapshotStartHour) {
        this.automatedSnapshotStartHour = automatedSnapshotStartHour;
    }

    @Override
    public void copyFrom(SnapshotOptions model) {
        setAutomatedSnapshotStartHour(model.automatedSnapshotStartHour());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SnapshotOptions toSnapshotOptions() {
        return SnapshotOptions.builder().automatedSnapshotStartHour(getAutomatedSnapshotStartHour()).build();
    }
}
