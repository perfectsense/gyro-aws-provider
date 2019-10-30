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

package gyro.aws.docdb;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.docdb.model.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DocDbFinder<C extends SdkClient, M, R extends AwsResource> extends AwsFinder<C, M, R> {

    public List<Filter> createDocDbFilters(Map<String, String> filters) {
        return filters.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

}
