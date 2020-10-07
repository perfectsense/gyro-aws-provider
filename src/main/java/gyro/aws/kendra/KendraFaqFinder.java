/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.kendra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.DescribeFaqResponse;
import software.amazon.awssdk.services.kendra.model.ListFaqsResponse;

/**
 * Query faq.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    kendra-faq: $(external-query aws::kendra-faq { id: "3f90903e-8348-48a2-8844-3f40582ba7cb", index-id: "1b149509-a587-415b-9a29-7f6eb3863eb2" })
 */
@Type("kendra-faq")
public class KendraFaqFinder extends AwsFinder<KendraClient, DescribeFaqResponse, KendraFaqResource> {

    private String id;
    private String indexId;

    /**
     * The id of the data source.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The id of the index associated with the data source.
     */
    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    @Override
    protected List<DescribeFaqResponse> findAllAws(KendraClient client) {
        throw new IllegalArgumentException("Cannot query faqs without 'index-id'.");
    }

    @Override
    protected List<DescribeFaqResponse> findAws(KendraClient client, Map<String, String> filters) {
        List<DescribeFaqResponse> faqs = new ArrayList<>();

        if (filters.containsKey("index-id")) {
            String indexId = filters.get("index-id");

            if (!filters.containsKey("id")) {
                ListFaqsResponse listFaqsResponse = client.listFaqs(r -> r.indexId(indexId));

                if (listFaqsResponse.hasFaqSummaryItems()) {
                    faqs = listFaqsResponse.faqSummaryItems()
                        .stream()
                        .map(r -> client.describeFaq(d -> d.id(r.id()).indexId(indexId)))
                        .collect(Collectors.toList());
                }

            } else {
                DescribeFaqResponse faq = client.describeFaq(d -> d.id(filters.get("id"))
                    .indexId(indexId));

                if (faq != null) {
                    faqs = Collections.singletonList(faq);
                }
            }

        } else {
            throw new IllegalArgumentException("Cannot query faqs without 'index-id'.");
        }

        return faqs;
    }
}
