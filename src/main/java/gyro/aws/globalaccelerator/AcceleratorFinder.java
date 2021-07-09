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
import software.amazon.awssdk.services.globalaccelerator.model.Accelerator;
import software.amazon.awssdk.services.globalaccelerator.model.AcceleratorNotFoundException;

/**
 * Query Global Accelerators.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    accelerator: $(external-query aws::global-accelerator { arn: ''})
 */
@Type("global-accelerator")
public class AcceleratorFinder extends AwsFinder<GlobalAcceleratorClient, Accelerator, AcceleratorResource> {

    private String arn;

    /**
     * The arn of the accelerator to look up.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Accelerator> findAllAws(GlobalAcceleratorClient client) {
        return client.listAcceleratorsPaginator().accelerators().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Accelerator> findAws(GlobalAcceleratorClient client, Map<String, String> filters) {
        List<Accelerator> accelerators = new ArrayList<>();

        try {
            accelerators.add(client.describeAccelerator(r -> r.acceleratorArn(filters.get("arn"))).accelerator());
        } catch (AcceleratorNotFoundException ex) {
            // Ignore
        }

        return accelerators;
    }
}
