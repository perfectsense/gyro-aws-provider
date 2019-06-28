package gyro.aws.cognitoidp;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import com.psddev.dari.util.CompactMap;
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
    public void create() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        CreateUserPoolResponse response = client.createUserPool(r -> r.poolName(getName())
            .userPoolTags(getTags()));

        setArn(response.userPool().arn());
        setId(response.userPool().id());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete() {
        CognitoIdentityProviderClient client = createClient(CognitoIdentityProviderClient.class);

        client.deleteUserPool(r -> r.userPoolId(getId()));
    }

    @Override
    public String toDisplayString() {
        return "user pool " + getName();
    }
}
