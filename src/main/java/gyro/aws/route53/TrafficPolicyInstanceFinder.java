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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ListTrafficPolicyInstancesRequest;
import software.amazon.awssdk.services.route53.model.ListTrafficPolicyInstancesResponse;
import software.amazon.awssdk.services.route53.model.NoSuchTrafficPolicyInstanceException;
import software.amazon.awssdk.services.route53.model.TrafficPolicyInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query traffic policy instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    traffic-policy-instance: $(external-query aws::route53-traffic-policy-instance { id: ''})
 */
@Type("route53-traffic-policy-instance")
public class TrafficPolicyInstanceFinder extends AwsFinder<Route53Client, TrafficPolicyInstance, TrafficPolicyInstanceResource> {
    private String id;

    /**
     * The ID of the traffic policy instance.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<TrafficPolicyInstance> findAllAws(Route53Client client) {
        List<TrafficPolicyInstance> trafficPolicyInstances = new ArrayList<>();

        String marker = null;
        ListTrafficPolicyInstancesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listTrafficPolicyInstances();
            } else {
                response = client.listTrafficPolicyInstances(ListTrafficPolicyInstancesRequest.builder().trafficPolicyInstanceNameMarker(marker).build());
            }

            marker = response.trafficPolicyInstanceNameMarker();
            trafficPolicyInstances.addAll(response.trafficPolicyInstances());
        } while (response.isTruncated());

        return trafficPolicyInstances;
    }

    @Override
    protected List<TrafficPolicyInstance> findAws(Route53Client client, Map<String, String> filters) {
        List<TrafficPolicyInstance> trafficPolicyInstances = new ArrayList<>();

        try {
            trafficPolicyInstances.add(client.getTrafficPolicyInstance(r -> r.id(filters.get("id"))).trafficPolicyInstance());
        } catch (NoSuchTrafficPolicyInstanceException ignore) {
            // ignore
        }

        return trafficPolicyInstances;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
