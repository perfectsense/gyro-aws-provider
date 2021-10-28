/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.clientconfiguration;

import gyro.aws.clientconfiguration.retrypolicy.RetryPolicy;

public class ClientOverrideConfiguration implements ClientConfigurationInterface {

    private String apiCallTimeout;
    private RetryPolicy retryPolicy;

    public String getApiCallTimeout() {
        return apiCallTimeout;
    }

    public void setApiCallTimeout(String apiCallTimeout) {
        this.apiCallTimeout = apiCallTimeout;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public void validate() {
        if (getApiCallTimeout() != null) {
            ClientConfigurationUtils.validate(getApiCallTimeout(), "api-call-timeout");
        }
    }

    public software.amazon.awssdk.core.client.config.ClientOverrideConfiguration toClientOverrideConfiguration() {
        software.amazon.awssdk.core.client.config.ClientOverrideConfiguration.Builder builder = software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
            .builder();

        if (getApiCallTimeout() != null) {
            builder.apiCallTimeout(ClientConfigurationUtils.getDuration(getApiCallTimeout()));
        }

        if (getRetryPolicy() != null) {
            builder.retryPolicy(getRetryPolicy().toRetryPolicy());
        }

        return builder.build();
    }
}
