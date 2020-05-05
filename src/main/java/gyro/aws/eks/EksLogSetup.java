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

package gyro.aws.eks;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.LogSetup;
import software.amazon.awssdk.services.eks.model.LogType;

public class EksLogSetup extends Diffable implements Copyable<LogSetup> {

    private Set<String> logTypes;

    /**
     * The set of available cluster control plane log types to enable for the cluster.
     */
    @Required
    public Set<String> getLogTypes() {
        if (logTypes == null) {
            logTypes = new HashSet<>();
        }

        return logTypes;
    }

    public void setLogTypes(Set<String> logTypes) {
        this.logTypes = logTypes;
    }

    @Override
    public void copyFrom(LogSetup model) {
        setLogTypes(new HashSet<>(model.types().stream().map(LogType::toString).collect(Collectors.toSet())));
    }

    @Override
    public String primaryKey() {
        return getLogTypes().toString();
    }

    LogSetup toLogSeup() {
        return LogSetup.builder()
            .enabled(true)
            .types(getLogTypes().stream().map(LogType::fromValue).collect(Collectors.toSet()))
            .build();
    }
}
