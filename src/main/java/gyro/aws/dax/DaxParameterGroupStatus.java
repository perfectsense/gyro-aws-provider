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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.ParameterGroupStatus;

public class DaxParameterGroupStatus extends Diffable implements Copyable<ParameterGroupStatus> {

    private List<String> nodeIdsToReboot;
    private String parameterApplyStatus;
    private String parameterGroupName;

    /**
     * The list of node IDs to be rebooted.
     */
    public List<String> getNodeIdsToReboot() {
        if (nodeIdsToReboot == null) {
            nodeIdsToReboot = new ArrayList<>();
        }

        return nodeIdsToReboot;
    }

    public void setNodeIdsToReboot(List<String> nodeIdsToReboot) {
        this.nodeIdsToReboot = nodeIdsToReboot;
    }

    /**
     * The status of the parameter group updates.
     */
    public String getParameterApplyStatus() {
        return parameterApplyStatus;
    }

    public void setParameterApplyStatus(String parameterApplyStatus) {
        this.parameterApplyStatus = parameterApplyStatus;
    }

    /**
     * The name of the parameter group.
     */
    public String getParameterGroupName() {
        return parameterGroupName;
    }

    public void setParameterGroupName(String parameterGroupName) {
        this.parameterGroupName = parameterGroupName;
    }

    @Override
    public void copyFrom(ParameterGroupStatus model) {
        setNodeIdsToReboot(model.nodeIdsToReboot());
        setParameterApplyStatus(model.parameterApplyStatus());
        setParameterGroupName(model.parameterGroupName());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
