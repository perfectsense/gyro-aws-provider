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

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.IPSet;
import software.amazon.awssdk.services.wafv2.model.ListIpSetsRequest;
import software.amazon.awssdk.services.wafv2.model.ListIpSetsResponse;
import software.amazon.awssdk.services.wafv2.model.Scope;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

@Type("wafv2-ip-set")
public class IpSetFinder extends AwsFinder<Wafv2Client, IPSet, IpSetResource> {

    private String id;
    private String name;
    private String scope;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    protected List<IPSet> findAllAws(Wafv2Client client) {
        List<IPSet> ipSets = new ArrayList<>();
        ListIpSetsResponse response;
        String marker = null;

        do {
            response = client.listIPSets(ListIpSetsRequest.builder()
                .scope(Scope.CLOUDFRONT)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            ipSets.addAll(response.ipSets()
                .stream()
                .map(o -> getIpSet(client, o.id(), o.name(), Scope.CLOUDFRONT.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        marker = null;

        do {
            response = client.listIPSets(ListIpSetsRequest.builder()
                .scope(Scope.REGIONAL)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            ipSets.addAll(response.ipSets()
                .stream()
                .map(o -> getIpSet(client, o.id(), o.name(), Scope.REGIONAL.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        return ipSets;
    }

    @Override
    protected List<IPSet> findAws(Wafv2Client client, Map<String, String> filters) {
        List<IPSet> ipSets = new ArrayList<>();

        IPSet ipSet = getIpSet(client, filters.get("id"), filters.get("name"), filters.get("scope"));

        if (ipSet != null) {
            ipSets.add(ipSet);
        }

        return ipSets;
    }

    private IPSet getIpSet(Wafv2Client client, String id, String name, String scope) {
        try {
            return client.getIPSet(r -> r.id(id).name(name).scope(scope)).ipSet();
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }
}
