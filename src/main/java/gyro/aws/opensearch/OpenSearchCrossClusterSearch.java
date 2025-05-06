/*
 * Copyright 2025, Brightspot.
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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.model.CrossClusterSearchConnectionProperties;
import software.amazon.awssdk.services.opensearch.model.SkipUnavailableStatus;

public class OpenSearchCrossClusterSearch extends Diffable implements Copyable<CrossClusterSearchConnectionProperties> {

    private String skipUnavailable;

    /**
     * The direct connection property to skip unavailable clusters. ``Defaults to 'true'``
     */
    @ValidStrings({"ENABLED", "DISABLED"})
    public String getSkipUnavailable() {
        if (skipUnavailable == null) {
            skipUnavailable = String.valueOf(SkipUnavailableStatus.ENABLED);
        }
        return skipUnavailable;
    }

    public void setSkipUnavailable(String skipUnavailable) {
        this.skipUnavailable = skipUnavailable;
    }

    @Override
    public void copyFrom(CrossClusterSearchConnectionProperties model) {
        setSkipUnavailable(model.skipUnavailableAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
