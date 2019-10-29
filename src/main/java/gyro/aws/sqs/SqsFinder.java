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

package gyro.aws.sqs;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;
import java.util.Map;

/**
 * Query SQS queues.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sqs: $(external-query aws::sqs-queue { name: ''})
 */
@Type("sqs-queue")
public class SqsFinder extends AwsFinder<SqsClient, String, SqsResource> {

    private String name;

    /**
     * The name of the queue.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<String> findAws(SqsClient client, Map<String, String> filters) {
        return client.listQueues(r -> r.queueNamePrefix(filters.get("name"))).queueUrls();
    }

    @Override
    protected List<String> findAllAws(SqsClient client) {
        return client.listQueues().queueUrls();
    }
}
