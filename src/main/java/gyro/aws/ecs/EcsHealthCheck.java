/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.HealthCheck;

public class EcsHealthCheck extends Diffable {

    private List<String> command;
    private Integer interval;
    private Integer timeout;
    private Integer retries;
    private Integer startPeriod;

    /**
     * A list of strings representing the command that the container runs to determine if it is healthy. (Required)
     * The list must start with ``CMD`` to execute the command arguments directly, or ``CMD-SHELL`` to run the command with the container's default shell.
     */
    @Required
    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }

        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    /**
     * The time period in seconds between each health check execution.
     * Valid values range from 5 to 300. Defaults to 30.
     */
    @Range(min = 5, max = 300)
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * The time period in seconds to wait for a health check to succeed before it is considered a failure.
     * Valid values range from 2 to 60. Defaults to 5.
     */
    @Range(min = 2, max = 60)
    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * The number of times to retry a failed health check before the container is considered unhealthy.
     * Valid values range from 1 to 10. Defaults to 3.
     */
    @Range(min = 1, max = 10)
    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    /**
     * The optional grace period within which to provide containers time to bootstrap before failed health checks count towards the maximum number of retries.
     * If a health check succeeds within the ``start-period``, then the container is considered healthy and any subsequent failures count toward the maximum number of retries.
     * Valid values range from 0 to 300. This parameter is disabled by default.
     */
    @Range(min = 0, max = 300)
    public Integer getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(Integer startPeriod) {
        this.startPeriod = startPeriod;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(HealthCheck model) {
        setCommand(model.command());
        setInterval(model.interval());
        setTimeout(model.timeout());
        setRetries(model.retries());
        setStartPeriod(model.startPeriod());
    }

    public HealthCheck copyTo() {
        return HealthCheck.builder()
            .command(getCommand())
            .interval(getInterval())
            .timeout(getTimeout())
            .retries(getRetries())
            .startPeriod(getStartPeriod())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getCommand().get(0).equals("CMD") && !getCommand().get(0).equals("CMD-SHELL")) {
            errors.add(new ValidationError(
               this,
               "command",
               "The 'command' must start with either 'CMD' or 'CMD-SHELL'."
            ));
        }

        return errors;
    }
}
