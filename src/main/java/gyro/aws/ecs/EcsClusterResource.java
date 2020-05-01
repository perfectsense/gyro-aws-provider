package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ClusterNotFoundException;
import software.amazon.awssdk.services.ecs.model.ClusterSetting;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.Tag;

@Type("ecs-cluster")
public class EcsClusterResource extends AwsResource implements Copyable<Cluster> {

    private String clusterName;
    private Set<EcsCapacityProviderResource> capacityProviders;
    private List<EcsCapacityProviderStrategyItem> defaultCapacityProviderStrategy;
    private Map<String, String> settings;
    private Map<String, String> tags;
    private String arn;

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
    public Set<EcsCapacityProviderResource> getCapacityProviders() {
        if (capacityProviders == null) {
            capacityProviders = new HashSet<>();
        }

        return capacityProviders;
    }

    public void setCapacityProviders(Set<EcsCapacityProviderResource> capacityProviders) {
        this.capacityProviders = capacityProviders;
    }

    /**
     * @subresource gyro.aws.ecs.EcsCapacityProviderStrategyItem
     */
    @Updatable
    public List<EcsCapacityProviderStrategyItem> getDefaultCapacityProviderStrategy() {
        if (defaultCapacityProviderStrategy == null) {
            defaultCapacityProviderStrategy = new ArrayList<>();
        }

        return defaultCapacityProviderStrategy;
    }

    public void setDefaultCapacityProviderStrategy(List<EcsCapacityProviderStrategyItem> defaultCapacityProviderStrategy) {
        this.defaultCapacityProviderStrategy = defaultCapacityProviderStrategy;
    }

    @Updatable
    public Map<String, String> getSettings() {
        if (settings == null) {
            settings = new HashMap<>();
        }

        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Cluster model) {
        setClusterName(model.clusterName());
        setCapacityProviders(
            model.capacityProviders().stream()
                .map(name -> findById(EcsCapacityProviderResource.class, name))
                .collect(Collectors.toSet())
        );
        setDefaultCapacityProviderStrategy(
            model.defaultCapacityProviderStrategy().stream().map(o -> {
                EcsCapacityProviderStrategyItem newItem = newSubresource(EcsCapacityProviderStrategyItem.class);
                newItem.copyFrom(o);
                return newItem;
            }).collect(Collectors.toList())
        );
        setSettings(
            model.settings().stream()
                .collect(Collectors.toMap(ClusterSetting::nameAsString, ClusterSetting::value))
        );
        setTags(
            model.tags().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value))
        );
        setArn(model.clusterArn());
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
                .settings(getSettings().entrySet().stream()
                    .map(o -> ClusterSetting.builder().name(o.getKey()).value(o.getValue()).build())
                    .collect(Collectors.toList()))
                .tags(getTags().entrySet().stream()
                    .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
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

        if (!getSettings().isEmpty()) {
            client.updateClusterSettings(
                r -> r.cluster(getClusterName()).settings(getSettings().entrySet().stream()
                    .map(o -> ClusterSetting.builder().name(o.getKey()).value(o.getValue()).build())
                    .collect(Collectors.toList()))
            );
        }

        EcsClusterResource currentResource = (EcsClusterResource) current;
        List<String> removeKeys = currentResource.getTags().keySet().stream()
            .filter(k -> !getTags().containsKey(k))
            .collect(Collectors.toList());

        if (!removeKeys.isEmpty()) {
            client.untagResource(r -> r.resourceArn(getArn()).tagKeys());
        }

        if (!getTags().isEmpty()) {
            client.tagResource(
                r -> r.resourceArn(getArn()).tags(getTags().entrySet().stream()
                    .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
                    .collect(Collectors.toList()))
            );
        }

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isUpdated(client));
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
            DescribeClustersResponse response = client.describeClusters(
                r -> r.clusters(getClusterName()).includeWithStrings("ATTACHMENTS", "SETTINGS")
            );

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

    private boolean isUpdated(EcsClient client) {
        Cluster ecsCluster = getCluster(client);

        if (ecsCluster == null) {
            System.out.println("\nCluster not found");
            return true;
        }

        if (ecsCluster.attachmentsStatus().equals("UPDATE_FAILED")) {
            System.out.println("\nThe capacity provider updates failed.");
        }

        return (
            ecsCluster.status().equals("ACTIVE") && !ecsCluster.attachmentsStatus().equals("UPDATE_IN_PROGRESS")
        );
    }

    private boolean isDeleted(EcsClient client) {
        Cluster ecsCluster = getCluster(client);

        return (ecsCluster == null);
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("default-capacity-provider-strategy")) {
            Set<String> providerNames = getCapacityProviders().stream()
                .map(EcsCapacityProviderResource::getName)
                .collect(Collectors.toSet());

            boolean baseDefined = false;
            boolean nonzeroWeight = false;

            for (EcsCapacityProviderStrategyItem item : getDefaultCapacityProviderStrategy()) {
                if (!providerNames.contains(item.primaryKey())) {
                    errors.add(new ValidationError(
                        this,
                        "default-capacity-provider-strategy",
                        "Capacity providers must be associated with the cluster in order to be added to the default capacity provider strategy.")
                    );
                }

                if (item.getBase() != 0) {
                    if (!baseDefined) {
                        baseDefined = true;
                    } else {
                        errors.add(new ValidationError(
                            this,
                            "default-capacity-provider-strategy",
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
                    "default-capacity-provider-strategy",
                    "The default capacity provider strategy must contain at least one item with a weight greater than 0."
                ));
            }
        }

        return errors;
    }
}
