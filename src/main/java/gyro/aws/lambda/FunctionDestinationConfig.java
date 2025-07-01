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
import software.amazon.awssdk.services.lambda.model.DestinationConfig;

public class FunctionDestinationConfig extends Diffable implements Copyable<DestinationConfig> {

    private FunctionOnSuccessConfig onSuccess;
    private FunctionOnFailureConfig onFailure;

    /**
     * The ARN of the destination for successful invocations.
     */
    @Updatable
    public FunctionOnSuccessConfig getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(FunctionOnSuccessConfig onSuccess) {
        this.onSuccess = onSuccess;
    }

    /**
     * The ARN of the destination for failed invocations.
     */
    @Updatable
    public FunctionOnFailureConfig getOnFailure() {
        return onFailure;
    }

    public void setOnFailure(FunctionOnFailureConfig onFailure) {
        this.onFailure = onFailure;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DestinationConfig model) {
        if (model.onFailure() != null) {
            FunctionOnFailureConfig config = newSubresource(FunctionOnFailureConfig.class);
            config.copyFrom(model.onFailure());
            setOnFailure(config);
        }

        if (model.onSuccess() != null) {
            FunctionOnSuccessConfig config = newSubresource(FunctionOnSuccessConfig.class);
            config.copyFrom(model.onSuccess());
            setOnSuccess(config);
        }
    }

    DestinationConfig toDestinationConfig() {
        DestinationConfig.Builder builder = DestinationConfig.builder();

        if (getOnSuccess() != null) {
            builder.onSuccess(getOnSuccess().toOnSuccess());
        }

        if (getOnFailure() != null) {
            builder.onFailure(getOnFailure().toOnFailure());
        }

        return builder.build();
    }
}
