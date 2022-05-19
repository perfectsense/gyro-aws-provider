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

package gyro.aws.eventbridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.GyroException;
import gyro.core.Type;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.ListRulesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListRulesResponse;
import software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException;
import software.amazon.awssdk.services.eventbridge.model.Rule;

/**
 * Query Event Bus Rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    event-bus-rule: $(external-query aws::event-bus-rule { event-bus-name: 'default'})
 */
@Type("event-bus-rule")
public class EventBusRuleFinder extends AwsFinder<EventBridgeClient, Rule, EventBusRuleResource> {

    private String eventBusName;
    private String rulePrefix;

    /**
     * The name of the event bus.
     */
    public String getEventBusName() {
        return eventBusName;
    }

    public void setEventBusName(String eventBusName) {
        this.eventBusName = eventBusName;
    }

    /**
     * The prefix for the event bus rule.
     */
    public String getRulePrefix() {
        return rulePrefix;
    }

    public void setRulePrefix(String rulePrefix) {
        this.rulePrefix = rulePrefix;
    }

    @Override
    protected List<Rule> findAllAws(EventBridgeClient client) {
        throw new UnsupportedOperationException("event-bus-rule cannot be searched without filters!!");
    }

    @Override
    protected List<Rule> findAws(EventBridgeClient client, Map<String, String> filters) {
        List<Rule> rules = new ArrayList<>();
        try {
            if (!filters.containsKey("event-bus-name")) {
                throw new GyroException("'event-bus-name' is required !!");
            }

            ListRulesRequest.Builder builder = ListRulesRequest.builder().eventBusName(filters.get("event-bus-name"));

            if (filters.containsKey("rule-prefix")) {
                builder = builder.namePrefix(filters.get("rule-prefix"));
            }

            ListRulesResponse response;
            String token = "";

            do {
                if (StringUtils.isBlank(token)) {
                    response = client.listRules(builder.build());
                } else {
                    response = client.listRules(builder.nextToken(token).build());
                }

                token = response.nextToken();

                if (response.rules() != null) {
                    rules = response.rules();
                }
            } while (!StringUtils.isBlank(token));
        } catch (ResourceNotFoundException ex) {
            // Ignore
        }

        return rules;
    }
}
