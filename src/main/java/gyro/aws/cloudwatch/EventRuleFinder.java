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

package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query event rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    event-rule: $(external-query aws::cloudwatch-event-rule { name: 'scale up rule' })
 */
@Type("cloudwatch-event-rule")
public class EventRuleFinder extends AwsFinder<CloudWatchEventsClient, Rule, EventRuleResource> {
    private String name;

    /**
     * The name of the rule.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Rule> findAllAws(CloudWatchEventsClient client) {
        List<Rule> rules = new ArrayList<>();
        String marker = null;
        ListRulesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRules();
            } else {
                response = client.listRules(ListRulesRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            rules.addAll(response.rules());
        } while (!ObjectUtils.isBlank(marker));

        return rules;
    }

    @Override
    protected List<Rule> findAws(CloudWatchEventsClient client, Map<String, String> filters) {
        return client.listRules(r -> r.namePrefix(filters.get("name"))).rules();
    }
}
