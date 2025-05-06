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
import software.amazon.awssdk.services.opensearch.model.ConnectionProperties;

public class OpenSearchConnectionProperties extends Diffable implements Copyable<ConnectionProperties> {

    private OpenSearchCrossClusterSearch crossClusterSearch;
    private String endpoint;


    /**
     * The cross-cluster search configuration for an OpenSearch domain.
     *
     * @subresource gyro.aws.opensearch.OpenSearchCrossClusterSearch
     */
    public OpenSearchCrossClusterSearch getCrossClusterSearch() {
        return crossClusterSearch;
    }

    public void setCrossClusterSearch(OpenSearchCrossClusterSearch crossClusterSearch) {
        this.crossClusterSearch = crossClusterSearch;
    }

    /**
     * The endpoint for the OpenSearch domain.
     */
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void copyFrom(ConnectionProperties model) {
        setEndpoint(model.endpoint());

        setCrossClusterSearch(null);
        if (model.crossClusterSearch() != null) {
            OpenSearchCrossClusterSearch crossClusterSearch = new OpenSearchCrossClusterSearch();
            crossClusterSearch.copyFrom(model.crossClusterSearch());
            setCrossClusterSearch(crossClusterSearch);
        }
    }


    @Override
    public String primaryKey() {
        return "";
    }
}
