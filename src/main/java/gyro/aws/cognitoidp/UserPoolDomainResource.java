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
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolDomainResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DomainDescriptionType;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cognito-user-pool-domain domain
 *         domain: "domainsecond"
 *         user-pool: $(aws::cognito-user-pool cognito)
 *     end
 */
@Type("cognito-user-pool-domain")
public class UserPoolDomainResource extends AwsResource implements Copyable<DomainDescriptionType> {

    private String certificateArn;
    private String domain;
    private UserPoolResource userPool;

    /**
     *  The certificate arn for the subdomain of the custom domain. (Optional)
     */
    @Updatable
    public String getCertificateArn() {
        return certificateArn;
    }

    public void setCertificateArn(String certificateArn) {
        this.certificateArn = certificateArn;
    }

    /**
     *  The domain. (Required)
     */
    @Required
    @Updatable
    @Id
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     *  The id of the user pool. (Required)
     */
    @Required
    public UserPoolResource getUserPool() {
        return userPool;
    }

    public void setUserPool(UserPoolResource userPool) {
        this.userPool = userPool;
    }

    @Override
    public void copyFrom(DomainDescriptionType model) {
        setCertificateArn(model.customDomainConfig().certificateArn());
        setDomain(model.domain());
        setUserPool(findById(UserPoolResource.class, model.userPoolId()));
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolDomainResponse response = client.describeUserPoolDomain(r -> r.domain(getDomain()));

            this.copyFrom(response.domainDescription());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        if (getCertificateArn() != null) {
            client.createUserPoolDomain(r -> r.domain(getDomain())
                    .userPoolId(getUserPool().getId())
                    .customDomainConfig(c -> c.certificateArn(getCertificateArn())));
        } else {
            client.createUserPoolDomain(r -> r.domain(getDomain())
                    .userPoolId(getUserPool().getId()));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.updateUserPoolDomain(r -> r.domain(getDomain())
                .userPoolId(getUserPool().getId()));

        if (getCertificateArn() != null) {
            client.updateUserPoolDomain(r -> r.customDomainConfig(c -> c.certificateArn(getCertificateArn())));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPoolDomain(r -> r.domain(getDomain())
                                            .userPoolId(getUserPool().getId()));
    }

}
