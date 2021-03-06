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

package gyro.aws.neptune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.DBInstance;
import software.amazon.awssdk.services.neptune.model.DbInstanceNotFoundException;
import software.amazon.awssdk.services.neptune.model.Filter;

/**
 * Query neptune instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    instance: $(external-query aws::neptune-instance { db-instance-identifier: 'neptune-instance-example' })
 */
@Type("neptune-instance")
public class NeptuneInstanceFinder extends AwsFinder<NeptuneClient, DBInstance, NeptuneInstanceResource> {

    private String dbClusterId;
    private String engine;
    private String dbInstanceIdentifier;

    /**
     * The identifier or arn of the cluster to which the instance belongs.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The name of the engine by which the cluster was created.
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The unique name of the Neptune instance.
     */
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    @Override
    protected List<DBInstance> findAllAws(NeptuneClient client) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.builder().name("engine").values("neptune").build());

        return client.describeDBInstances(r -> r.filters(filters)).dbInstances().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBInstance> findAws(NeptuneClient client, Map<String, String> filters) {
        if (!filters.containsKey("engine")) {
            filters.put("engine", "neptune");
        }

        Map<String, String> recognizedFilters = new HashMap<>(filters);
        if (filters.containsKey("db-instance-identifier")) {
            recognizedFilters.remove("db-instance-identifier");
        }

        try {
            return client.describeDBInstances(r -> r.filters(createNeptuneFilters(recognizedFilters))).dbInstances()
                .stream()
                .filter(o -> !filters.containsKey("db-instance-identifier") || o.dbInstanceIdentifier().equals(filters.get("db-instance-identifier")))
                .collect(Collectors.toList());

        } catch (DbInstanceNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
