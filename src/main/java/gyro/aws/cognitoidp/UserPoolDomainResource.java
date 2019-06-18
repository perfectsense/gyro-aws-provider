package gyro.aws.cognitoidp;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

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
 *     aws::user-pool-domain domain
 *         domain: "domainsecond"
 *         user-pool: $(aws::user-pool cognito)
 *     end
 */
@Type("user-pool-domain")
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
    public void create() {
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
    public void update(Resource current, Set<String> changedFieldNames) {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.updateUserPoolDomain(r -> r.domain(getDomain())
                .userPoolId(getUserPool().getId()));

        if (getCertificateArn() != null) {
            client.updateUserPoolDomain(r -> r.customDomainConfig(c -> c.certificateArn(getCertificateArn())));
        }
    }

    @Override
    public void delete() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPoolDomain(r -> r.domain(getDomain())
                                            .userPoolId(getUserPool().getId()));
    }

    @Override
    public String toDisplayString() {
        return "user pool domain " + getDomain();
    }
}
