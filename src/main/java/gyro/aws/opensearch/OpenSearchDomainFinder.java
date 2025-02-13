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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainsRequest;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;
import software.amazon.awssdk.services.opensearch.model.ResourceNotFoundException;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Query elasticsearch domain
 *
 * Example
 * -------
 *
 * .. code-block::gyro
 *
 *    opensearch-domain: $(external-query aws::opensearch-domain {domain-name: "testdomain"})
 */
@Type("opensearch-domain")
public class OpenSearchDomainFinder
    extends AwsFinder<OpenSearchClient, DomainStatus, OpenSearchDomainResource> {

    private String domainName;

    /**
     * The name of the OpenSearch Domain.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    protected List<DomainStatus> findAllAws(OpenSearchClient client) {
        return client.describeDomains(DescribeDomainsRequest.builder()
            .domainNames(client.listDomainNames(SdkBuilder::build)
                .domainNames()
                .stream()
                .map(DomainInfo::domainName)
                .collect(
                    Collectors.toList()))
            .build()).domainStatusList();
    }

    @Override
    protected List<DomainStatus> findAws(
        OpenSearchClient client, Map<String, String> filters) {
        try {
            return client.describeDomains(r -> r.domainNames(filters.get("domain-name"))).domainStatusList();
        } catch (ResourceNotFoundException ex) {
            return new ArrayList<>();
        }
    }
}
