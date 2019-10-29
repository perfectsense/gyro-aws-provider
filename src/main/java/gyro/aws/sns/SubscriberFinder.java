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

package gyro.aws.sns;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query SNS subscriptions.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    subscriber: $(external-query aws::sns-subscriber { arn: ''})
 */
@Type("sns-subscriber")
public class SubscriberFinder extends AwsFinder<SnsClient, Subscription, SubscriberResource> {

    private String arn;

    /**
     * The arn of the subscription.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Subscription> findAws(SnsClient client, Map<String, String> filters) {
        List<Subscription> targetSubscription = new ArrayList<>();

        if (filters.containsKey("arn") && !ObjectUtils.isBlank(filters.get("arn"))) {
            targetSubscription.addAll(client.listSubscriptionsPaginator()
                .subscriptions().stream()
                .filter(o -> o.subscriptionArn().equals(filters.get("arn")))
                .collect(Collectors.toList()));
        }

        return targetSubscription;
    }

    @Override
    protected List<Subscription> findAllAws(SnsClient client) {
        return client.listSubscriptionsPaginator().subscriptions().stream().collect(Collectors.toList());
    }
}
