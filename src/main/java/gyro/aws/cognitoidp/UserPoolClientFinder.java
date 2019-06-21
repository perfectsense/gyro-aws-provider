package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query user pool clients.
 *
 * .. code-block:: gyro
 *
 *    user-pool-client: $(aws::user-pool-client EXTERNAL/* | user-pool-id = '' and id = '')
 */
@Type("user-pool-client")
public class UserPoolClientFinder extends AwsFinder<CognitoIdentityProviderClient, UserPoolClientType, UserPoolClientResource> {

    private String clientId;
    private String userPoolId;

    /**
     *  The user pool client id.
     */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
        if (!filters.containsKey("user-pool-id")) {
            throw new IllegalArgumentException("'user-pool-id' is required.");
        }

        if (!filters.containsKey("client-id")) {
            throw new IllegalArgumentException("'client-id' is required.");
        }

        try {
            return Collections.singletonList(client.describeUserPoolClient(r -> r.userPoolId(filters.get("user-pool-id")).clientId(filters.get("client-id"))).userPoolClient());
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<UserPoolClientType> findAllAws(CognitoIdentityProviderClient client) {
        List<UserPoolClientType> poolClients = new ArrayList<>();

        String poolToken;
        do {
            ListUserPoolsResponse listUserPoolsResponse = client.listUserPools(r -> r.maxResults(60));
            for (UserPoolDescriptionType descriptionType : listUserPoolsResponse.userPools()) {
                ListUserPoolClientsResponse listUserPoolClientsResponse = client.listUserPoolClients(r -> r.userPoolId(descriptionType.id()).maxResults(60));

                String clientToken;
                do {
                    for (UserPoolClientDescription clients : listUserPoolClientsResponse.userPoolClients()) {
                        poolClients.add(client.describeUserPoolClient(r -> r.clientId(clients.clientId()).userPoolId(clients.userPoolId())).userPoolClient());
                    }

                    clientToken = listUserPoolClientsResponse.nextToken();
                } while (clientToken != null);
            }

            poolToken = listUserPoolsResponse.nextToken();
        } while (poolToken != null);

        return poolClients;
    }
}
