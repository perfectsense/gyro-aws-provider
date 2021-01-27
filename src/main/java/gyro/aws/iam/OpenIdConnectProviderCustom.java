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

package gyro.aws.iam;

import software.amazon.awssdk.services.iam.model.GetOpenIdConnectProviderResponse;

public class OpenIdConnectProviderCustom {
    private final GetOpenIdConnectProviderResponse provider;
    private final String arn;

    public GetOpenIdConnectProviderResponse getProvider() {
        return provider;
    }

    public String getArn() {
        return arn;
    }

    public OpenIdConnectProviderCustom(GetOpenIdConnectProviderResponse provider, String arn) {
        this.provider = provider;
        this.arn = arn;
    }
}
