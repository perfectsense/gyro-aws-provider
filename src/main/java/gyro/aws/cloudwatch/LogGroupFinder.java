/*
 * Copyright 2026, Brightspot.
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
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query log group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    log-group: $(external-query aws::cloudwatch-log-group { name: '/app/api' })
 */
@Type("cloudwatch-log-group")
public class LogGroupFinder extends AwsFinder<CloudWatchLogsClient, LogGroup, LogGroupResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<LogGroup> findAllAws(CloudWatchLogsClient client) {
        List<LogGroup> resources = new ArrayList<>();

        String nextToken = null;
        do {
            DescribeLogGroupsResponse response = client.describeLogGroups(
                DescribeLogGroupsRequest.builder()
                    .nextToken(nextToken)
                    .build()
            );

            if (response.logGroups() != null) {
                for (LogGroup logGroup : response.logGroups()) {
                    resources.add(logGroup);
                }
            }

            nextToken = response.nextToken();
        } while (nextToken != null);

        return resources;
    }

    @Override
    protected List<LogGroup> findAws(CloudWatchLogsClient client, Map<String, String> filters) {
        List<LogGroup> resources = new ArrayList<>();

        String name = filters != null ? filters.get("name") : null;

        if (name != null && !name.isEmpty()) {
            LogGroup logGroup = getLogGroup(client, name);
            if (logGroup != null) {
                resources.add(logGroup);
            }
        } else {
            resources = findAllAws(client);
        }

        return resources;
    }

    public static LogGroup getLogGroup(CloudWatchLogsClient client, String logGroupName) {
        try {
            DescribeLogGroupsResponse response = client.describeLogGroups(
                DescribeLogGroupsRequest.builder()
                    .logGroupNamePrefix(logGroupName)
                    .build()
            );

            if (response.logGroups() != null) {
                for (LogGroup logGroup : response.logGroups()) {
                    if (logGroup.logGroupName().equals(logGroupName)) {
                        return logGroup;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
