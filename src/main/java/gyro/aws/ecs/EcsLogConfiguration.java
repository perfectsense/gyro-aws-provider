package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.LogConfiguration;
import software.amazon.awssdk.services.ecs.model.LogDriver;

public class EcsLogConfiguration extends Diffable {

    private String logDriver;
    private Map<String, String> options;

    /**
     * The log driver to use for the container. (Required)
     * Valid values are ``json-file``, ``syslog``, ``journald``, ``gelf``, ``fluentd``, ``awslogs``, ``splunk``, ``awsfirelens``, ``logentries``, and ``sumologic``.
     * When using the Fargate launch type, the only supported log drivers are ``awslogs``, ``splunk``, and ``awsfirelens``.
     */
    @Required
    @ValidStrings({"json-file", "syslog", "journald", "gelf", "fluentd", "awslogs", "splunk", "awsfirelens", "logentries", "sumologic"})
    public String getLogDriver() {
        return logDriver;
    }

    public void setLogDriver(String logDriver) {
        this.logDriver = logDriver;
    }

    /**
     * The configuration options to send to the log driver.
     */
    public Map<String, String> getOptions() {
        if (options == null) {
            options = new HashMap<>();
        }

        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public void copyFrom(LogConfiguration model) {
        setLogDriver(model.logDriverAsString());
        setOptions(model.options());
    }

    public LogConfiguration copyTo() {
        return LogConfiguration.builder()
            .logDriver(getLogDriver())
            .options(getOptions()).build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        EcsTaskDefinitionResource taskDefinition = ((EcsContainerDefinition) parent()).getParentTaskDefinition();

        if (taskDefinition.getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
            if (!getLogDriver().equals(LogDriver.AWSLOGS.toString()) && !getLogDriver().equals(LogDriver.AWSFIRELENS.toString()) && !getLogDriver().equals(LogDriver.SPLUNK.toString())) {
                errors.add(new ValidationError(
                    this,
                    "log-driver",
                    "When the task definition's 'requires-compatibilities' parameter contains 'FARGATE', the 'log-driver' must be 'awslogs', 'splunk', or 'awsfirelens'"
                ));
            }
        }

        return errors;
    }
}
