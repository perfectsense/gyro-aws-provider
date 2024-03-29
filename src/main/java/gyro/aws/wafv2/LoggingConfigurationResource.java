/*
 * Copyright 2020, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;

public class LoggingConfigurationResource extends Diffable implements Copyable<LoggingConfiguration> {

    private Set<FieldToMatchResource> redactedField;
    private Set<String> logDestinationConfigs;

    /**
     * The set of field match setting to take out of logging.
     *
     * @subresource gyro.aws.wafv2.FieldToMatchResource
     */
    @Updatable
    public Set<FieldToMatchResource> getRedactedField() {
        if (redactedField == null) {
            redactedField = new HashSet<>();
        }

        return redactedField;
    }

    public void setRedactedField(Set<FieldToMatchResource> redactedField) {
        this.redactedField = redactedField;
    }

    /**
     * A set of arn of AWS Kinesis Data Firehouse to associate with the web acl.
     */
    @Updatable
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
    public String primaryKey() {
        return "";
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getLogDestinationConfigs().isEmpty() && getRedactedField().isEmpty()) {
            errors.add(new ValidationError(
                this,
                null,
                "At least one of 'redacted-field' or 'log-destination-configs' must be set."));
        }

        return errors;

    }
}
