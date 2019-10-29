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
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query hosted zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    hosted-zone: $(external-query aws::route53-hosted-zone { hosted-zone-id: ''})
 */
@Type("route53-hosted-zone")
public class HostedZoneFinder extends AwsFinder<Route53Client, HostedZone, HostedZoneResource> {
    private String hostedZoneId;

    /**
     * The ID of the hosted zone.
     */
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    @Override
    protected List<HostedZone> findAllAws(Route53Client client) {
        return client.listHostedZonesPaginator().hostedZones().stream().collect(Collectors.toList());
    }

    @Override
    protected List<HostedZone> findAws(Route53Client client, Map<String, String> filters) {
        List<HostedZone> hostedZones = new ArrayList<>();

        try {
            hostedZones.add(client.getHostedZone(r -> r.id(filters.get("hosted-zone-id"))).hostedZone());
        } catch (HostedZoneNotFoundException ignore) {
            // ignore
        }

        return hostedZones;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
