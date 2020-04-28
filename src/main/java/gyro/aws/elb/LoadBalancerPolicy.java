package gyro.aws.elb;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyAttributeDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyDescription;

public class LoadBalancerPolicy extends Diffable implements Copyable<PolicyDescription> {

    private String type;
    private Set<String> enabledAttributes;

    /**
     * The type of the policy. (Required)
     */
    @Required
    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * A set of enabled attributes for the policy. (Required)
     */
    @Required
    @Updatable
    public Set<String> getEnabledAttributes() {
        if (enabledAttributes == null) {
            enabledAttributes = new HashSet<>();
        }

        return enabledAttributes;
    }

    public void setEnabledAttributes(Set<String> enabledAttributes) {
        this.enabledAttributes = enabledAttributes;
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    @Override
    public void copyFrom(PolicyDescription description) {
        setType(description.policyTypeName());

        getEnabledAttributes().clear();
        setEnabledAttributes(description.policyAttributeDescriptions()
            .stream().filter(o -> o.attributeValue().equals("true")).map(
                PolicyAttributeDescription::attributeName).collect(
                Collectors.toSet()));
    }
}
