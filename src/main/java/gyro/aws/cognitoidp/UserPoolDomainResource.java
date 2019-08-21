package gyro.aws.cognitoidp;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolDomainResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DomainDescriptionType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
        throw new NotImplementedException();
    }

    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

}
