package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("cognitoidp-user-pool")
public class UserPoolFinder extends AwsFinder<CognitoIdentityProviderClient, UserPoolType, UserPoolResource> {

    private String id;

    /**
     *  The user pool client name.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<UserPoolType> findAws(CognitoIdentityProviderClient client, Map<String, String> filters) {
        List<UserPoolType> userPoolType = new ArrayList<>();

        userPoolType.add(client.describeUserPool(r -> r.userPoolId(filters.get("id"))).userPool());

        return userPoolType;
    }

    @Override
    public List<UserPoolType> findAllAws(CognitoIdentityProviderClient client) {
        List<UserPoolType> allPools = new ArrayList<>();

        for (UserPoolDescriptionType descriptionType : client.listUserPools(r -> r.maxResults(60)).userPools()) {
            allPools.add(client.describeUserPool(r -> r.userPoolId(descriptionType.id())).userPool());
        }

        return allPools;
    }
}
