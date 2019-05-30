package gyro.aws.cognitoidp;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DomainDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("cognitoidp-user-pool-domain")
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
        List<DomainDescriptionType> domainDescriptionType = new ArrayList<>();

        domainDescriptionType.add(client.describeUserPoolDomain(r -> r.domain(filters.get("domain"))).domainDescription());

        return domainDescriptionType;
    }

    @Override
    protected List<DomainDescriptionType> findAllAws(CognitoIdentityProviderClient client) {
        List<DomainDescriptionType> domainDescriptionType = new ArrayList<>();

        for (UserPoolDescriptionType descriptionType : client.listUserPools(r -> r.maxResults(60)).userPools()) {
            UserPoolType userPoolType = client.describeUserPool(r -> r.userPoolId(descriptionType.id())).userPool();
            domainDescriptionType.add(client.describeUserPoolDomain(r -> r.domain(userPoolType.domain())).domainDescription());
        }

        return domainDescriptionType;
    }
}
