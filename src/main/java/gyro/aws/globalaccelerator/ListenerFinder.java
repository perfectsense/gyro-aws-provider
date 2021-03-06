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
import software.amazon.awssdk.services.globalaccelerator.model.Listener;

/**
 * Query Global Accelerators Listeners.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator-listener: $(external-query aws::global-accelerator-listener { accelerator-arn: ''})
 */
@Type("global-accelerator-listener")
public class ListenerFinder extends AwsFinder<GlobalAcceleratorClient, Listener, ListenerResource> {

    private String acceleratorArn;

    /**
     * The arn of the accelerator to look up listeners on.
     */
    public String getAcceleratorArn() {
        return acceleratorArn;
    }

    public void setAcceleratorArn(String acceleratorArn) {
        this.acceleratorArn = acceleratorArn;
    }

    @Override
    protected List<Listener> findAllAws(GlobalAcceleratorClient client) {
        return new ArrayList<>();
    }

    @Override
    protected List<Listener> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        return client.listListenersPaginator(r -> r.acceleratorArn(filters.get("accelerator-arn")))
            .listeners()
            .stream()
            .collect(Collectors.toList());
    }
}
