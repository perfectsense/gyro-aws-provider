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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query metric alarm.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    metric-alarm: $(external-query aws::cloudwatch-metric-alarm { name: 'scale up rule' })
 */
@Type("cloudwatch-metric-alarm")
public class MetricAlarmFinder extends AwsFinder<CloudWatchClient, MetricAlarm, MetricAlarmResource> {
    private String name;
    private String alarmPrefix;
    private String state;
    private String actionPrefix;

    /**
     * The name of the alarm. Cannot be specified if 'alarm-prefix' specified.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The prefix of the alarm. Cannot be specified if 'name' specified.
     */
    public String getAlarmPrefix() {
        return alarmPrefix;
    }

    public void setAlarmPrefix(String alarmPrefix) {
        this.alarmPrefix = alarmPrefix;
    }

    /**
     * The state of the alarm.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The action prefix of the alarm.
     */
    public String getActionPrefix() {
        return actionPrefix;
    }

    public void setActionPrefix(String actionPrefix) {
        this.actionPrefix = actionPrefix;
    }

    @Override
    protected List<MetricAlarm> findAllAws(CloudWatchClient client) {
        return client.describeAlarmsPaginator().metricAlarms().stream().collect(Collectors.toList());
    }

    @Override
    protected List<MetricAlarm> findAws(CloudWatchClient client, Map<String, String> filters) {
        if (filters.containsKey("name") && filters.containsKey("alarm-prefix")) {
            throw new IllegalArgumentException("Either 'name' or 'alarm-prefix' can be specified, not both.");
        }

        return client.describeAlarmsPaginator(
            r -> r.alarmNames(filters.get("name"))
                .alarmNamePrefix(filters.get("alarm-prefix"))
                .actionPrefix(filters.get("action-prefix"))
                .stateValue(filters.get("state"))).metricAlarms().stream().collect(Collectors.toList());
    }
}
