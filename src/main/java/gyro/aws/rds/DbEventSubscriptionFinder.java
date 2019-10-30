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

package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.EventSubscription;
import software.amazon.awssdk.services.rds.model.SubscriptionNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db event subscription.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    subscriptions: $(external-query aws::db-event-subscription { name: 'db-event-subscription-example'})
 */
@Type("db-event-subscription")
public class DbEventSubscriptionFinder extends AwsFinder<RdsClient, EventSubscription, DbEventSubscriptionResource> {

    private String name;

    /**
     * The name of the event subscription.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<EventSubscription> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeEventSubscriptions(r -> r.subscriptionName(filters.get("name"))).eventSubscriptionsList();
        } catch (SubscriptionNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<EventSubscription> findAllAws(RdsClient client) {
        return client.describeEventSubscriptionsPaginator().eventSubscriptionsList().stream().collect(Collectors.toList());
    }
}
