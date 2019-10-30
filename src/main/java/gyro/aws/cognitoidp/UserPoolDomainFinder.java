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

package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DomainDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query user pool clients.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    user-pool-domain: $(external-query aws::cognito-user-pool-domain { domain: 'domain'})
 */
@Type("cognito-user-pool-domain")
public class UserPoolDomainFinder extends AwsFinder<CognitoIdentityProviderClient, DomainDescriptionType, UserPoolDomainResource> {

    private String domain;

    /**
     *  The domain of the user pool.
     */
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public List<DomainDescriptionType> findAws(CognitoIdentityProviderClient client, Map<String, String> filters) {
        if (!filters.containsKey("domain")) {
            throw new IllegalArgumentException("'domain' is required.");
        }

        try {
            return Collections.singletonList(client.describeUserPoolDomain(r -> r.domain(filters.get("domain"))).domainDescription());
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DomainDescriptionType> findAllAws(CognitoIdentityProviderClient client) {
        List<DomainDescriptionType> domainDescriptionType = new ArrayList<>();

        String token;
        do {
            ListUserPoolsResponse listUserPoolsResponse = client.listUserPools(r -> r.maxResults(60));
            for (UserPoolDescriptionType descriptionType : listUserPoolsResponse.userPools()) {
                UserPoolType userPoolType = client.describeUserPool(r -> r.userPoolId(descriptionType.id())).userPool();
                domainDescriptionType.add(client.describeUserPoolDomain(r -> r.domain(userPoolType.domain())).domainDescription());
            }

            token = listUserPoolsResponse.nextToken();
        } while (token != null);

        return domainDescriptionType;
    }
}
