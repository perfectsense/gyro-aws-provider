package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;

import com.psddev.dari.util.CompactMap;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;

import java.util.Map;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::authenticate-cognito-user-pool cognito
 *         user-pool-name: "user pool name"
 *     end
 */
@ResourceType("authenticate-cognito-user-pool")
public class AuthenticateCognitoUserPoolResource extends AwsResource {

    private Map<String, String> tags;
    private String userPoolArn;
    private String userPoolId;
    private String userPoolName;

    /**
     *  List of tags associated with the alb (Optional)
     */
    @ResourceUpdatable
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

    @ResourceOutput
    public String getUserPoolArn() {
        return userPoolArn;
    }

    public void setUserPoolArn(String userPoolArn) {
        this.userPoolArn = userPoolArn;
    }

    @ResourceOutput
    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public String getUserPoolName() {
        return userPoolName;
    }

    public void setUserPoolName(String userPoolName) {
        this.userPoolName = userPoolName;
    }

    @Override
    public boolean refresh() {
        try {
            CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

            DescribeUserPoolResponse response = client.describeUserPool(r -> r.userPoolId(getUserPoolId()));

            setUserPoolArn(response.userPool().arn());
            setUserPoolId(response.userPool().id());
            setTags(response.userPool().userPoolTags());

            return true;
        } catch (CognitoIdentityProviderException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void create() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        CreateUserPoolResponse response = client.createUserPool(r -> r.poolName(getUserPoolName())
            .userPoolTags(getTags()));

        setUserPoolArn(response.userPool().arn());
        setUserPoolId(response.userPool().id());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPool(r -> r.userPoolId(getUserPoolId()));
    }

    @Override
    public String toDisplayString() {
        return "user pool " + getUserPoolName();
    }
}
