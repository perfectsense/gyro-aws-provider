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

package gyro.aws.ec2;

import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.PlacementGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query placement groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    pg: $(external-query aws::placement-group {name: "placement-group-example", state: "available", strategy: "spread" })
 */
@Type("placement-group")
public class PlacementGroupFinder extends Ec2TaggableAwsFinder<Ec2Client, PlacementGroup, PlacementGroupResource> {

    private String name;
    private String state;
    private String strategy;

    /**
     * The name of the Placement Group.
     */
    @Filter("group-name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The state of the Placement Group. Valid values are ``pending``, ``available``, ``deleting`` or ``deleted``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Approaches towards managing the placement of instances on the underlying hardware. Valid values are ``cluster``, ``spread`` or ``partition``.
     */
    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @Override
    protected List<PlacementGroup> findAllAws(Ec2Client client) {
        return client.describePlacementGroups().placementGroups().stream().collect(Collectors.toList());
    }

    @Override
    protected List<PlacementGroup> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describePlacementGroups(r -> r.filters(createFilters(filters))).placementGroups().stream().collect(Collectors.toList());
    }
}
