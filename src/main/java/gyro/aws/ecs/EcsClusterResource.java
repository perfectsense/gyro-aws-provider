package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ClusterNotFoundException;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;

@Type("ecs-cluster")
public class EcsClusterResource extends AwsResource implements Copyable<Cluster> {

    private String clusterName;
    private List<EcsCapacityProviderResource> capacityProviders;
    private List<EcsCapacityProviderStrategyItem> defaultCapacityProviderStrategy;

    @Required
    @Id
    @Regex(value = "^[a-zA-Z]([-a-zA-Z0-9]{0,254})?", message = "1 to 255 letters, numbers, and hyphens. Must begin with a letter.")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Updatable
    public List<EcsCapacityProviderResource> getCapacityProviders() {
        return capacityProviders;
    }

    public void setCapacityProviders(List<EcsCapacityProviderResource> capacityProviders) {
        this.capacityProviders = capacityProviders;
    }

    /**
     * @subresource gyro.aws.ecs.EcsCapacityProviderStrategyItem
     */
    @Updatable
    public List<EcsCapacityProviderStrategyItem> getDefaultCapacityProviderStrategy() {
        return defaultCapacityProviderStrategy;
    }

    public void setDefaultCapacityProviderStrategy(List<EcsCapacityProviderStrategyItem> defaultCapacityProviderStrategy) {
        this.defaultCapacityProviderStrategy = defaultCapacityProviderStrategy;
    }

    @Override
    public void copyFrom(Cluster model) {
        setClusterName(model.clusterName());
        setCapacityProviders(
            model.capacityProviders().stream()
                .map(name -> findById(EcsCapacityProviderResource.class, name))
                .collect(Collectors.toList())
        );
        setDefaultCapacityProviderStrategy(
            model.defaultCapacityProviderStrategy().stream().map(o -> {
                EcsCapacityProviderStrategyItem newItem = new EcsCapacityProviderStrategyItem();
                newItem.copyFrom(o);
                return newItem;
            }).collect(Collectors.toList())
        );
    }

    @Override
    public boolean refresh() {
        EcsClient client = createClient(EcsClient.class);

        Cluster ecsCluster = getCluster(client);

        if (ecsCluster == null) {
            return false;
        }

        copyFrom(ecsCluster);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.createCluster(
            r -> r.clusterName(getClusterName())
                .capacityProviders(getCapacityProviders().stream()
                    .map(EcsCapacityProviderResource::getName)
                    .collect(Collectors.toList()))
                .defaultCapacityProviderStrategy(getDefaultCapacityProviderStrategy().stream()
                    .map(EcsCapacityProviderStrategyItem::copyTo)
                    .collect(Collectors.toList()))
        );

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.putClusterCapacityProviders(
            r -> r.cluster(getClusterName())
                .capacityProviders(getCapacityProviders().stream()
                    .map(EcsCapacityProviderResource::getName)
                    .collect(Collectors.toList()))
                .defaultCapacityProviderStrategy(getDefaultCapacityProviderStrategy().stream()
                    .map(EcsCapacityProviderStrategyItem::copyTo)
                    .collect(Collectors.toList()))
        );

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.deleteCluster(r -> r.cluster(getClusterName()));

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isDeleted(client));
    }

    private Cluster getCluster(EcsClient client) {
        if (ObjectUtils.isBlank(getClusterName())) {
            throw new GyroException("name is missing, unable to load cluster.");
        }

        Cluster ecsCluster = null;

        try {
            DescribeClustersResponse response = client.describeClusters(r -> r.clusters(getClusterName()));

            if (response.hasClusters()) {
                ecsCluster = response.clusters().get(0);
            }
        } catch (ClusterNotFoundException ex) {
            // ignore
        }

        return ecsCluster;
    }

    private boolean isActive(EcsClient client) {
        Cluster ecsCluster = getCluster(client);

        return (ecsCluster != null && ecsCluster.status().equals("ACTIVE"));
    }

    private boolean isDeleted(EcsClient client) {
        Cluster ecsCluster = getCluster(client);

        return (ecsCluster == null);
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("defaultCapacityProviderStrategy")) {
            Set<String> providerNames = getCapacityProviders().stream()
                .map(EcsCapacityProviderResource::getName)
                .collect(Collectors.toSet());

            boolean baseDefined = false;
            boolean nonzeroWeight = false;

            for (EcsCapacityProviderStrategyItem item : getDefaultCapacityProviderStrategy()) {
                if (!providerNames.contains(item.primaryKey())) {
                    errors.add(new ValidationError(
                        this,
                        "defaultCapacityProviderStrategy",
                        "Capacity providers must be associated with the cluster in order to be added to the default capacity provider strategy.")
                    );
                }

                if (item.getBase() != 0) {
                    if (!baseDefined) {
                        baseDefined = true;
                    } else {
                        errors.add(new ValidationError(
                            this,
                            "defaultCapacityProviderStrategy",
                            "The default capacity provider strategy cannot have more than one item with a defined/nonzero base."
                        ));
                    }
                }

                if (item.getWeight() != 0) {
                    nonzeroWeight = true;
                }
            }

            if (!nonzeroWeight) {
                errors.add(new ValidationError(
                    this,
                    "defaultCapacityProviderStrategy",
                    "The default capacity provider strategy must contain at least one item with a weight greater than 0."
                ));
            }
        }

        return errors;
    }
}
