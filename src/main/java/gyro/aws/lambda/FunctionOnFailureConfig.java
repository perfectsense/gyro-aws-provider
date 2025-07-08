/*
 * Copyright 2025, Brightspot.
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

package gyro.aws.lambda;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.lambda.model.OnFailure;

public class FunctionOnFailureConfig extends Diffable implements Copyable<OnFailure> {

    private String destination;

    /**
     * The ARN of the destination for failed invocations.
     */
    @Updatable
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public void copyFrom(OnFailure model) {
        setDestination(model.destination());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    OnFailure toOnFailure() {
        return OnFailure.builder()
            .destination(getDestination())
            .build();
    }
}
