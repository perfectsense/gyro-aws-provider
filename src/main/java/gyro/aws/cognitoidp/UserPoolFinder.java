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
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query user pools.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    user-pool: $(external-query aws::cognito-user-pool {id: 'us-east-1_xxxxxxx' })
 */
@Type("cognito-user-pool")
public class UserPoolFinder extends AwsFinder<CognitoIdentityProviderClient, UserPoolType, UserPoolResource> {

    private String id;

    /**
     *  The user pool id.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<UserPoolType> findAws(CognitoIdentityProviderClient client, Map<String, String> filters) {
        if (!filters.containsKey("id")) {
            throw new IllegalArgumentException("'id' is required.");
        }

        try {
            return Collections.singletonList(client.describeUserPool(r -> r.userPoolId(filters.get("id"))).userPool());
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<UserPoolType> findAllAws(CognitoIdentityProviderClient client) {
        List<UserPoolType> allPools = new ArrayList<>();

        String token;
        do {
            ListUserPoolsResponse listUserPoolsResponse = client.listUserPools(r -> r.maxResults(60));
            for (UserPoolDescriptionType descriptionType : listUserPoolsResponse.userPools()) {
                allPools.add(client.describeUserPool(r -> r.userPoolId(descriptionType.id())).userPool());
            }

            token = listUserPoolsResponse.nextToken();
        } while (token != null);

        return allPools;
    }
}
