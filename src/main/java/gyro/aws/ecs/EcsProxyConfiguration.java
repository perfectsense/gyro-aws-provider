package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.ProxyConfiguration;
import software.amazon.awssdk.services.ecs.model.ProxyConfigurationType;

public class EcsProxyConfiguration extends Diffable {

    private ProxyConfigurationType type;
    private String containerName;
    private Map<String, String> properties;

    /**
     * The proxy type. (Required)
     * The only valid value is ``APPMESH``.
     */
    @Required
    public ProxyConfigurationType getType() {
        return type;
    }

    public void setType(ProxyConfigurationType type) {
        this.type = type;
    }

    /**
     * The name of the container that will serve as the App Mesh proxy. (Required)
     */
    @Required
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * The set of network configuration parameters to provide the Container Network Interface (CNI) plugin, specified as key-value pairs. (Required)
     *
     * Valid keys include:
     *      ``IgnoredUID`` - (Required) The user ID (UID) of the proxy container as defined by the ``user`` parameter in a ``container-definition``. This is used to ensure the proxy ignores its own traffic. If ``IgnoredGID`` is specified, this parameter may be excluded.
     *      ``IgnoredGID`` - (Required) The group ID (GID) of the proxy container as defined by the ``user`` parameter in a ``container-definition``. This is used to ensure the proxy ignores its own traffic. If ``IgnoredUID`` is specified, this parameter may be excluded.
     *      ``AppPorts`` - (Required) The list of ports that the application uses. Network traffic to these ports is forwarded to the ``ProxyIngressPort`` and ``ProxyEgressPort``.
     *      ``ProxyIngressPort`` - (Required) Specifies the port that incoming traffic to the ``AppPorts`` is directed to.
     *      ``ProxyEgressPort`` - (Required) Specifies the port that outgoing traffic from the ``AppPorts`` is directed to.
     *      ``EgressIgnoredPorts`` - The egress traffic going to the specified ports is ignored and not redirected to the ``ProxyEgressPort``.
     *      ``EgressIgnoredIPs`` - The egress traffic going to the specified IP addresses is ignored and not redirected to the ``ProxyEgressPort``.
     * Custom properties may be specified as well.
     */
    @Required
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }

        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(ProxyConfiguration model) {
        setType(model.type());
        setContainerName(model.containerName());
        setProperties(
            model.properties().stream()
                .collect(Collectors.toMap(KeyValuePair::name, KeyValuePair::value))
        );
    }

    public ProxyConfiguration copyTo() {
        return ProxyConfiguration.builder()
            .type(getType())
            .containerName(getContainerName())
            .properties(getProperties().entrySet().stream()
                .map(o -> KeyValuePair.builder().name(o.getKey()).value(o.getValue()).build())
                .collect(Collectors.toList()))
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        EcsTaskDefinitionResource taskDefinition = (EcsTaskDefinitionResource) parent();

        if (!getProperties().containsKey("IgnoredUID") && !getProperties().containsKey("IgnoredGID")) {
            errors.add(new ValidationError(
                this,
                "properties",
                "One of the parameters 'IgnoredUID' or 'IgnoredGID' must be specified in 'properties'."
            ));
        }

        for (String key : Arrays.asList("AppPorts", "ProxyIngressPort", "ProxyEgressPort")) {
            if (!getProperties().containsKey(key) || ObjectUtils.isBlank(getProperties().get(key))) {
                errors.add(new ValidationError(
                    this,
                    "properties",
                    "The parameter '" + key + "' must be specified in 'properties'."
                ));
            }
        }

        if (getType() == ProxyConfigurationType.APPMESH && taskDefinition.getNetworkMode() != NetworkMode.AWSVPC) {
            errors.add(new ValidationError(
                this,
                "type",
                "A proxy configuration with its 'type' set to 'APPMESH' is only supported when the task definition has its 'network-mode' set to 'awsvpc'."
            ));
        }

        return errors;
    }
}
