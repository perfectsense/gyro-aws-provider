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
 * .. code-block:: gyro
 *
 *    user-pool: $(aws::cognito-user-pool EXTERNAL/* | id = '')
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
