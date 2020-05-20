package gyro.aws.elb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyAttributeDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyDescription;

public class LoadBalancerPolicy extends Diffable implements Copyable<PolicyDescription> {

    private String type;
    private Set<String> enabledAttributes;
    private String predefinedPolicy;

    /**
     * The type of the policy.
     */
    @Updatable
    @ConflictsWith("predefined-policy")
    @DependsOn("enabled-attributes")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * A set of enabled attributes for the policy.
     */
    @Updatable
    @ConflictsWith("predefined-policy")
    @DependsOn("type")
    public Set<String> getEnabledAttributes() {
        if (enabledAttributes == null) {
            enabledAttributes = new HashSet<>();
        }

        return enabledAttributes;
    }

    public void setEnabledAttributes(Set<String> enabledAttributes) {
        this.enabledAttributes = enabledAttributes;
    }

    /**
     * The name of a predefined policy.
     */
    @Updatable
    @ConflictsWith({"type","enabled-attributes"})
    public String getPredefinedPolicy() {
        return predefinedPolicy;
    }

    public void setPredefinedPolicy(String predefinedPolicy) {
        this.predefinedPolicy = predefinedPolicy;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(PolicyDescription description) {
        PolicyAttributeDescription referenceAttribute = description.policyAttributeDescriptions()
            .stream()
            .filter(o -> o.attributeName().equals("Reference-Security-Policy"))
            .findFirst()
            .orElse(null);

        getEnabledAttributes().clear();
        if (referenceAttribute != null) {
            setPredefinedPolicy(referenceAttribute.attributeValue());
            setType(null);
        } else {
            setType(description.policyTypeName());

            setEnabledAttributes(description.policyAttributeDescriptions()
                .stream().filter(o -> o.attributeValue().equals("true")).map(
                    PolicyAttributeDescription::attributeName).collect(
                    Collectors.toSet()));
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        if (ObjectUtils.isBlank(getType()) && ObjectUtils.isBlank(getPredefinedPolicy())) {
            errors.add(new ValidationError(this, null, "Either 'type' and 'enabled-attribute' or 'predefined-policy' is required!"));
        }

        return errors;
    }
}
