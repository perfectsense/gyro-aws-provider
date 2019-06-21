package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DomainDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query user pool clients.
 *
 * .. code-block:: gyro
 *
 *    user-pool-domain: $(aws::user-pool-domain EXTERNAL/* | domain = '')
 */
@Type("user-pool-domain")
public class UserPoolDomainFinder extends AwsFinder<CognitoIdentityProviderClient, DomainDescriptionType, UserPoolDomainResource> {

    private String domain;

    /**
     *  The domain of the user pool.
     */
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public List<DomainDescriptionType> findAws(CognitoIdentityProviderClient client, Map<String, String> filters) {
        if (!filters.containsKey("domain")) {
            throw new IllegalArgumentException("'domain' is required.");
        }

        try {
            return Collections.singletonList(client.describeUserPoolDomain(r -> r.domain(filters.get("domain"))).domainDescription());
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DomainDescriptionType> findAllAws(CognitoIdentityProviderClient client) {
        List<DomainDescriptionType> domainDescriptionType = new ArrayList<>();

        String token;
        do {
            ListUserPoolsResponse listUserPoolsResponse = client.listUserPools(r -> r.maxResults(60));
            for (UserPoolDescriptionType descriptionType : listUserPoolsResponse.userPools()) {
                UserPoolType userPoolType = client.describeUserPool(r -> r.userPoolId(descriptionType.id())).userPool();
                domainDescriptionType.add(client.describeUserPoolDomain(r -> r.domain(userPoolType.domain())).domainDescription());
            }

            token = listUserPoolsResponse.nextToken();
        } while (token != null);

        return domainDescriptionType;
    }
}
