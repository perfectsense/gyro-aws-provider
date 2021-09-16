package gyro.aws.eks;

import gyro.core.Type;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.DiffableType;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfig;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.ListIdentityProviderConfigsResponse;
import software.amazon.awssdk.services.eks.model.NotFoundException;

@Type("eks-authentication")
public class EksStandaloneAuthenticationResource extends EksAuthentication {

    private EksClusterResource cluster;

    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        try {
            ListIdentityProviderConfigsResponse response = client.listIdentityProviderConfigs(r -> r
                .clusterName(clusterName()));
            if (response.hasIdentityProviderConfigs() && !response.identityProviderConfigs().isEmpty()) {
                IdentityProviderConfig providerConfig = response.identityProviderConfigs().get(0);

                IdentityProviderConfigResponse auth = EksAuthentication.getIdentityProviderConfigResponse(
                    client,
                    getName(),
                    providerConfig.name(),
                    providerConfig.type());

                if (auth != null) {
                    copyFrom(auth);
                }
            }
        } catch (NotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public String primaryKey() {
        String name = DiffableInternals.getName(this);
        return String.format("%s::%s", DiffableType.getInstance(getClass()).getName(), name);
    }

    @Override
    protected String clusterName() {
        return cluster.getName();
    }

}
