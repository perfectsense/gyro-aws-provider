package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;

public class LoggingConfigurationResource extends WafDiffable implements Copyable<LoggingConfiguration> {

    private Set<FieldToMatchResource> redactedField;
    private Set<String> logDestinationConfigs;

    public Set<FieldToMatchResource> getRedactedField() {
        if (redactedField == null) {
            redactedField = new HashSet<>();
        }

        return redactedField;
    }

    public void setRedactedField(Set<FieldToMatchResource> redactedField) {
        this.redactedField = redactedField;
    }

    public Set<String> getLogDestinationConfigs() {
        if (logDestinationConfigs == null) {
            logDestinationConfigs = new HashSet<>();
        }

        return logDestinationConfigs;
    }

    public void setLogDestinationConfigs(Set<String> logDestinationConfigs) {
        this.logDestinationConfigs = logDestinationConfigs;
    }

    @Override
    public void copyFrom(LoggingConfiguration loggingConfiguration) {
        getRedactedField().clear();
        if (loggingConfiguration.redactedFields() != null) {
            setRedactedField(loggingConfiguration.redactedFields().stream().map(o -> {
                FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
                fieldToMatch.copyFrom(o);
                return fieldToMatch;
            }).collect(Collectors.toSet()));
        }

        setLogDestinationConfigs(loggingConfiguration.logDestinationConfigs() != null
            ? new HashSet<>(loggingConfiguration.logDestinationConfigs())
            : null);
        setHashCode(loggingConfiguration.hashCode());
    }

    LoggingConfiguration toLoggingConfiguration() {
        WebAclResource parent = (WebAclResource) parent();

        LoggingConfiguration.Builder builder = LoggingConfiguration.builder().resourceArn(parent.getArn());

        if (!getRedactedField().isEmpty()) {
            builder = builder.redactedFields(getRedactedField().stream()
                .map(FieldToMatchResource::toFieldToMatch)
                .collect(Collectors.toList()));
        }

        if (!getLogDestinationConfigs().isEmpty()) {
            builder = builder.logDestinationConfigs(getLogDestinationConfigs());
        }

        return builder.build();
    }
}
