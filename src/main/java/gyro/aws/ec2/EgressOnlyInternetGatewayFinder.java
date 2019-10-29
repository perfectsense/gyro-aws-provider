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
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.EgressOnlyInternetGateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query egress only internet gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    egress-gateway: $(external-query aws::egress-only-internet-gateway { egress-only-internet-gateway-id: 'eigw-0f5c4f2180ecf5127'})
 */
@Type("egress-only-internet-gateway")
public class EgressOnlyInternetGatewayFinder extends AwsFinder<Ec2Client, EgressOnlyInternetGateway, EgressOnlyInternetGatewayResource> {
    private String egressOnlyInternetGatewayId;

    /**
     * The ID for the egress only internet gateway..
     */
    public String getEgressOnlyInternetGatewayId() {
        return egressOnlyInternetGatewayId;
    }

    public void setEgressOnlyInternetGatewayId(String egressOnlyInternetGatewayId) {
        this.egressOnlyInternetGatewayId = egressOnlyInternetGatewayId;
    }

    @Override
    protected List<EgressOnlyInternetGateway> findAllAws(Ec2Client client) {
        return client.describeEgressOnlyInternetGatewaysPaginator().egressOnlyInternetGateways().stream().collect(Collectors.toList());
    }

    @Override
    protected List<EgressOnlyInternetGateway> findAws(Ec2Client client, Map<String, String> filters) {
        if (filters.containsKey("egress-only-internet-gateway-id")) {
            return client.describeEgressOnlyInternetGatewaysPaginator(r -> r.egressOnlyInternetGatewayIds(filters.get("egress-only-internet-gateway-id"))).egressOnlyInternetGateways().stream().collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }
}
