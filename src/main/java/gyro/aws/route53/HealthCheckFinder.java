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

package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetHealthCheckResponse;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.NoSuchHealthCheckException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query route53 health check.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    health-check: $(external-query aws::route53-health-check { id: ''})
 */
@Type("route53-health-check")
public class HealthCheckFinder extends AwsFinder<Route53Client, HealthCheck, HealthCheckResource> {
    private String id;

    /**
     * The ID of the Health Check.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<HealthCheck> findAllAws(Route53Client client) {
        return client.listHealthChecksPaginator().healthChecks().stream().collect(Collectors.toList());
    }

    @Override
    protected List<HealthCheck> findAws(Route53Client client, Map<String, String> filters) {
        List<HealthCheck> healthChecks = new ArrayList<>();

        try {
            GetHealthCheckResponse response = client.getHealthCheck(r -> r.healthCheckId(filters.get("id")));
            healthChecks.add(response.healthCheck());
        } catch (NoSuchHealthCheckException ignore) {
            //ignore
        }

        return healthChecks;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
