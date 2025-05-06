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
     */
    public OpenSearchCrossClusterSearch getCrossClusterSearch() {
        return crossClusterSearch;
    }

    public void setCrossClusterSearch(OpenSearchCrossClusterSearch crossClusterSearch) {
        this.crossClusterSearch = crossClusterSearch;
    }

    /**
     * The endpoint for the OpenSearch domain.
     *
     * @return The endpoint URL as a string.
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

        OpenSearchCrossClusterSearch crossClusterSearch = new OpenSearchCrossClusterSearch();
        crossClusterSearch.setSkipUnavailable(model.crossClusterSearch().skipUnavailableAsString());
        setCrossClusterSearch(crossClusterSearch);
    }


    @Override
    public String primaryKey() {
        return "";
    }
}
