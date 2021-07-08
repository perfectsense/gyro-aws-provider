/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointGroup;

/**
 * Query Global Accelerators Listener Endpoint Groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator-endpoint-group: $(external-query aws::global-accelerator-endpoint-group { listener-arn: ''})
 */
@Type("global-accelerator-endpoint-group")
public class EndpointGroupFinder extends AwsFinder<GlobalAcceleratorClient, EndpointGroup, EndpointGroupResource> {

    private String listenerArn;

    /**
     * The arn of the accelerator listener to look up endpoint groups on.
     */
    public String getListenerArn() {
        return listenerArn;
    }

    public void setListenerArn(String listenerArn) {
        this.listenerArn = listenerArn;
    }

    @Override
    protected List<EndpointGroup> findAllAws(GlobalAcceleratorClient client) {
        return new ArrayList<>();
    }

    @Override
    protected List<EndpointGroup> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        return new ArrayList<>(client.listEndpointGroups(r -> r.listenerArn(filters.get("listener-arn")))
            .endpointGroups());
    }
}
