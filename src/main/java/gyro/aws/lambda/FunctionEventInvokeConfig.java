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
import software.amazon.awssdk.services.lambda.model.GetFunctionEventInvokeConfigResponse;
import software.amazon.awssdk.services.lambda.model.PutFunctionEventInvokeConfigRequest;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionEventInvokeConfigRequest;

public class FunctionEventInvokeConfig extends Diffable implements Copyable<GetFunctionEventInvokeConfigResponse> {

    private Integer maximumRetryAttempts;
    private Integer maximumEventAgeInSeconds;
    private FunctionDestinationConfig destinationConfig;

    /**
     * The maximum number of times to retry when the function returns an error.
     */
    @Updatable
    public Integer getMaximumRetryAttempts() {
        return maximumRetryAttempts;
    }

    public void setMaximumRetryAttempts(Integer maximumRetryAttempts) {
        this.maximumRetryAttempts = maximumRetryAttempts;
    }

    /**
     * The maximum age of a request that Lambda sends to a function for processing.
     */
    @Updatable
    public Integer getMaximumEventAgeInSeconds() {
        return maximumEventAgeInSeconds;
    }

    public void setMaximumEventAgeInSeconds(Integer maximumEventAgeInSeconds) {
        this.maximumEventAgeInSeconds = maximumEventAgeInSeconds;
    }

    /**
     * The destination configuration for the function version.
     *
     * @subresource gyro.aws.lambda.DestinationConfig
     */
    @Updatable
    public FunctionDestinationConfig getDestinationConfig() {
        return destinationConfig;
    }

    public void setDestinationConfig(FunctionDestinationConfig destinationConfig) {
        this.destinationConfig = destinationConfig;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(GetFunctionEventInvokeConfigResponse model) {
        setMaximumRetryAttempts(model.maximumRetryAttempts());
        setMaximumEventAgeInSeconds(model.maximumEventAgeInSeconds());

        setDestinationConfig(null);
        if (model.destinationConfig() != null) {
            FunctionDestinationConfig config = newSubresource(FunctionDestinationConfig.class);
            config.copyFrom(model.destinationConfig());
            setDestinationConfig(config);
        }
    }

    PutFunctionEventInvokeConfigRequest.Builder toPutFunctionEventInvokeConfigRequest() {
        PutFunctionEventInvokeConfigRequest.Builder builder = PutFunctionEventInvokeConfigRequest.builder();

        if (getMaximumRetryAttempts() != null) {
            builder.maximumRetryAttempts(getMaximumRetryAttempts());
        }

        if (getMaximumEventAgeInSeconds() != null) {
            builder.maximumEventAgeInSeconds(getMaximumEventAgeInSeconds());
        }

        if (getDestinationConfig() != null) {
            builder.destinationConfig(getDestinationConfig().toDestinationConfig());
        }

        return builder;
    }

    UpdateFunctionEventInvokeConfigRequest.Builder toUpdateFunctionEventInvokeConfigRequest() {
        UpdateFunctionEventInvokeConfigRequest.Builder builder = UpdateFunctionEventInvokeConfigRequest.builder();

        if (getMaximumRetryAttempts() != null) {
            builder.maximumRetryAttempts(getMaximumRetryAttempts());
        }

        if (getMaximumEventAgeInSeconds() != null) {
            builder.maximumEventAgeInSeconds(getMaximumEventAgeInSeconds());
        }

        if (getDestinationConfig() != null) {
            builder.destinationConfig(getDestinationConfig().toDestinationConfig());
        }

        return builder;
    }
}
