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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetOpenIdConnectProviderResponse;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.OpenIDConnectProviderListEntry;

/**
 * Query openid connect provider.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    openid-connect-provider: $(external-query aws::iam-openid-connect-provider { arn: ''})
 */
@Type("iam-openid-connect-provider")
public class OpenIdConnectProviderFinder extends AwsFinder<IamClient, OpenIdConnectProviderCustom, OpenIdConnectProviderResource> {

    private String arn;

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<OpenIdConnectProviderCustom> findAllAws(IamClient client) {
        List<OpenIdConnectProviderCustom> openIdConnectProviders = new ArrayList<>();
        try {
            List<String> arns = client.listOpenIDConnectProviders()
                .openIDConnectProviderList()
                .stream()
                .map(OpenIDConnectProviderListEntry::arn)
                .collect(Collectors.toList());

            openIdConnectProviders = arns.stream().map(arn -> {
                GetOpenIdConnectProviderResponse provider = client.getOpenIDConnectProvider(r -> r.openIDConnectProviderArn(
                    arn));
                return new OpenIdConnectProviderCustom(provider, arn);
            }).collect(Collectors.toList());
        } catch (NoSuchEntityException ignore) {
            // provider not found
        }

        return openIdConnectProviders;
    }

    @Override
    protected List<OpenIdConnectProviderCustom> findAws(
        IamClient client, Map<String, String> filters) {
        List<OpenIdConnectProviderCustom> openIdConnectProviders = new ArrayList<>();
        try {
            GetOpenIdConnectProviderResponse provider = client.getOpenIDConnectProvider(r -> r.openIDConnectProviderArn(
                filters.get("arn")));
            openIdConnectProviders.add(new OpenIdConnectProviderCustom(provider, filters.get("arn")));
        } catch (NoSuchEntityException ignore) {
            // provider not found
        }

        return openIdConnectProviders;
    }
}
