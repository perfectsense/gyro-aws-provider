/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.ecs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.CapacityProvider;
import software.amazon.awssdk.services.ecs.model.EcsException;

/**
 * Query ECS capacity provider.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-capacity-provider: $(external-query aws::ecs-capacity-provider { name: 'capacity-provider-example' })
 */
@Type("ecs-capacity-provider")
public class EcsCapacityProviderFinder extends AwsFinder<EcsClient, CapacityProvider, EcsCapacityProviderResource> {

    private String name;

    /**
     * The name of the capacity provider.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CapacityProvider> findAllAws(EcsClient client) {
        return client.describeCapacityProviders(r -> r.includeWithStrings("TAGS"))
            .capacityProviders().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CapacityProvider> findAws(EcsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeCapacityProviders(
                r -> r.capacityProviders(filters.get("name"))
                    .includeWithStrings("TAGS")
            ).capacityProviders().stream().collect(Collectors.toList());

        } catch (EcsException ex) {
            return Collections.emptyList();
        }
    }
}
