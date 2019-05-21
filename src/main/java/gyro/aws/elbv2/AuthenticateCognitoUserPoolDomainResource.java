package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.Updatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolDomainResponse;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::authenticate-cognito-user-pool-domain domain
 *         domain: "domainsecond"
 *         user-pool-id: $(aws::authenticate-cognito-user-pool cognito | user-pool-id)
 *     end
 */
@ResourceType("authenticate-cognito-user-pool-domain")
public class AuthenticateCognitoUserPoolDomainResource extends AwsResource {

    private String certificateArn;
    private String domain;
    private String userPoolId;

    @Updatable
    public String getCertificateArn() {
        return certificateArn;
    }

    public void setCertificateArn(String certificateArn) {
        this.certificateArn = certificateArn;
    }

    @Updatable
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolDomainResponse response = client.describeUserPoolDomain(r -> r.domain(getDomain()));

            setUserPoolId(response.domainDescription().userPoolId());
            setCertificateArn(response.domainDescription().customDomainConfig().certificateArn());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        if (getCertificateArn() != null) {
            client.createUserPoolDomain(r -> r.domain(getDomain())
                    .userPoolId(getUserPoolId())
                    .customDomainConfig(c -> c.certificateArn(getCertificateArn())));
        } else {
            client.createUserPoolDomain(r -> r.domain(getDomain())
                    .userPoolId(getUserPoolId()));
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.updateUserPoolDomain(r -> r.domain(getDomain())
                .userPoolId(getUserPoolId()));

        if (getCertificateArn() != null) {
            client.updateUserPoolDomain(r -> r.customDomainConfig(c -> c.certificateArn(getCertificateArn())));
        }
    }

    @Override
    public void delete() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPoolDomain(r -> r.domain(getDomain())
                                            .userPoolId(getUserPoolId()));
    }

    @Override
    public String toDisplayString() {
        return "user pool domain " + getDomain();
    }
}
