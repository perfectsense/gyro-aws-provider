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

package gyro.aws.elasticsearch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;
import software.amazon.awssdk.services.elasticsearch.model.DescribeElasticsearchDomainsRequest;
import software.amazon.awssdk.services.elasticsearch.model.DomainInfo;
import software.amazon.awssdk.services.elasticsearch.model.ElasticsearchDomainStatus;

/**
 * Query elasticsearch domain
 *
 * Example
 * -------
 *
 * .. code-block::gyro
 *
 *    elasticsearch-domain: $(external-query aws::elasticsearch-domain {domain-name: "testdomain"})
 */
@Type("elasticsearch-domain")
public class ElasticSearchDomainFinder
    extends AwsFinder<ElasticsearchClient, ElasticsearchDomainStatus, ElasticsearchDomainResource> {

    private String domainName;

    /**
     * The name of the Elasticsearch Domain.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    protected List<ElasticsearchDomainStatus> findAllAws(ElasticsearchClient client) {
        return client.describeElasticsearchDomains(DescribeElasticsearchDomainsRequest.builder()
            .domainNames(client.listDomainNames().domainNames().stream().map(DomainInfo::domainName).collect(
                Collectors.toList()))
            .build()).domainStatusList();
    }

    @Override
    protected List<ElasticsearchDomainStatus> findAws(
        ElasticsearchClient client, Map<String, String> filters) {
        return client.describeElasticsearchDomains(r -> r.domainNames(filters.get("domain-name"))).domainStatusList();
    }
}
