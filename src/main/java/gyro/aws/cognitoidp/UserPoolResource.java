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

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import com.psddev.dari.util.CompactMap;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.Map;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cognito-user-pool cognito
 *         name: "user pool name"
 *     end
 */
@Type("cognito-user-pool")
public class UserPoolResource extends AwsResource implements Copyable<UserPoolType> {

    private Map<String, String> tags;
    private String arn;
    private String id;
    private String name;

    /**
     *  List of tags associated with the user pool. (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    /**
     *  The arn of the user pool.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The id of the user pool.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *  The name of the user pool. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(UserPoolType model) {
        setArn(model.arn());
        setId(model.id());
        setName(model.name());
        setTags(model.userPoolTags());
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolResponse response = client.describeUserPool(r -> r.userPoolId(getId()));

            this.copyFrom(response.userPool());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        CreateUserPoolResponse response = client.createUserPool(r -> r.poolName(getName())
            .userPoolTags(getTags()));

        setArn(response.userPool().arn());
        setId(response.userPool().id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPool(r -> r.userPoolId(getId()));
    }

}
