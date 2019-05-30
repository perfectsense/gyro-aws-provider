package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("cognitoidp-user-pool-client")
public class UserPoolClientFinder extends AwsFinder<CognitoIdentityProviderClient, UserPoolClientType, UserPoolClientResource> {

    private String id;
    private String userPoolId;

    /**
     *  The user pool client id.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *  The id of the user pool.
     */
    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    @Override
    protected List<UserPoolClientType> findAws(CognitoIdentityProviderClient client, Map<String, String> filters) {
        List<UserPoolClientType> userPool = new ArrayList<>();

        userPool.add(client.describeUserPoolClient(r -> r.userPoolId(filters.get("user-pool-id")).clientId(filters.get("id"))).userPoolClient());

        return userPool;
    }

    @Override
    protected List<UserPoolClientType> findAllAws(CognitoIdentityProviderClient client) {
        List<UserPoolClientType> userPoolClientType = new ArrayList<>();

        for (UserPoolDescriptionType pool : client.listUserPools(r -> r.maxResults(60)).userPools()) {
            for (UserPoolClientDescription poolClient : client.listUserPoolClients(r -> r.maxResults(60)).userPoolClients()) {
                userPoolClientType.add(client.describeUserPoolClient(r -> r.userPoolId(pool.id()).clientId(poolClient.clientId())).userPoolClient());
            }
        }

        return userPoolClientType;
    }
}
