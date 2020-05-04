package gyro.aws.ecs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.CapacityProvider;
import software.amazon.awssdk.services.ecs.model.EcsException;

/**
 * Query ecs capacity provider.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-capacity-provider: $(external-query aws::ecs-capacity-provider { name: 'capacity-provider-example' })
 */
@Type("ecs-capacity-provider")
public class EcsCapacityProviderFinder extends AwsFinder<EcsClient, CapacityProvider, EcsCapacityProviderResource> {

    private String name;

    /**
     * The name of the capacity provider. Up to 255 characters are allowed, including letters, numbers, underscores, and hyphens.
     * The name cannot be prefixed with ``aws``, ``ecs``, or ``fargate``, regardless of character case.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CapacityProvider> findAllAws(EcsClient client) {
        return client.describeCapacityProviders(r -> r.includeWithStrings("TAGS"))
            .capacityProviders().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CapacityProvider> findAws(EcsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeCapacityProviders(
                r -> r.capacityProviders(filters.get("name"))
                    .includeWithStrings("TAGS")
            ).capacityProviders().stream().collect(Collectors.toList());

        } catch (EcsException ex) {
            return Collections.emptyList();
        }
    }
}
