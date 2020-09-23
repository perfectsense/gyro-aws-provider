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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.LogSetup;
import software.amazon.awssdk.services.eks.model.LogType;
import software.amazon.awssdk.services.eks.model.Logging;

public class EksLogging extends Diffable implements Copyable<Logging> {

    private List<EksLogSetup> enabledLogTypes;

    /**
     * The cluster control plane logging configuration for your cluster.
     */
    @Required
    @Updatable
    public List<EksLogSetup> getEnabledLogTypes() {
        if (enabledLogTypes == null) {
            enabledLogTypes = new ArrayList<>();
        }

        return enabledLogTypes;
    }

    public void setEnabledLogTypes(List<EksLogSetup> enabledLogTypes) {
        this.enabledLogTypes = enabledLogTypes;
    }

    @Override
    public void copyFrom(Logging model) {
        if (model.hasClusterLogging()) {
            getEnabledLogTypes().clear();
            model.clusterLogging().stream().filter(l -> l.enabled().equals(Boolean.TRUE)).forEach(l -> {
                EksLogSetup eksLogSetup = newSubresource(EksLogSetup.class);
                eksLogSetup.copyFrom(l);
                getEnabledLogTypes().add(eksLogSetup);
            });
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Logging toLogging() {
        List<LogSetup> logSetups = getEnabledLogTypes().stream().map(EksLogSetup::toLogSeup).collect(Collectors.toList());
        List<LogType> enabledLogTypes = new ArrayList<>();
        getEnabledLogTypes().forEach(e -> enabledLogTypes.addAll((e.getLogTypes().stream().map(LogType::fromValue).collect(Collectors.toList()))));
        List<LogType> logTypesToDisable = LogType.knownValues().stream().filter(e -> !enabledLogTypes.contains(e)).collect(Collectors.toList());
        logSetups.add(LogSetup.builder().types(logTypesToDisable).enabled(Boolean.FALSE).build());

        return Logging.builder()
                .clusterLogging(logSetups)
                .build();
    }
}
