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

package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import software.amazon.awssdk.core.SdkClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Ec2TaggableAwsFinder<C extends SdkClient, M, R extends AwsResource> extends AwsFinder<C, M, R> {

    @Override
    public final List<R> find(Map<String, Object> filters) {
        return findAws(newClient(), convertTags(filters)).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    /**
     * Convert {tagKey: tagValue} to {tag:Key: tagValue}
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> convertTags(Map<String, Object> query) {
        Map<String, String> filters = new HashMap<>();

        for (Map.Entry<String, Object> e : query.entrySet()) {
            if ("tag".equalsIgnoreCase(e.getKey()) && e.getValue() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) e.getValue();
                for (String key : map.keySet()) {
                    filters.put("tag:" + key, map.get(key).toString());
                }

            } else if (!(e.getValue() instanceof String)) {
                throw new GyroException("Unsupported type in filter: " + e.getValue().getClass());
            } else {
                filters.put(e.getKey(), e.getValue().toString());
            }
        }

        return filters;
    }
}
