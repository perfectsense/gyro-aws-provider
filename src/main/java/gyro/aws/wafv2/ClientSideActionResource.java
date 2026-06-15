/*
 * Copyright 2025, Brightspot.
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
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.ClientSideAction;
import software.amazon.awssdk.services.wafv2.model.Regex;
import software.amazon.awssdk.services.wafv2.model.SensitivityToAct;
import software.amazon.awssdk.services.wafv2.model.UsageOfAction;

public class ClientSideActionResource extends Diffable implements Copyable<ClientSideAction> {

    private List<RegexResource> exemptUriRegularExpressions;
    private SensitivityToAct sensitivity;
    private UsageOfAction usageOfAction;

    /**
     * List of URI patterns that are exempt from the client-side action.
     *
     * @subresource gyro.aws.wafv2.RegexResource
     */
    @Updatable
    public List<RegexResource> getExemptUriRegularExpressions() {
        if (exemptUriRegularExpressions == null) {
            exemptUriRegularExpressions = new ArrayList<>();
        }
        return exemptUriRegularExpressions;
    }

    public void setExemptUriRegularExpressions(List<RegexResource> exemptUriRegularExpressions) {
        this.exemptUriRegularExpressions = exemptUriRegularExpressions;
    }

    /**
     * Sensitivity level for the client-side action.
     */
    @Updatable
    @ValidStrings({ "LOW", "MEDIUM", "HIGH" })
    public SensitivityToAct getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(SensitivityToAct sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Usage configuration for the client-side action.
     */
    @Required
    @Updatable
    @ValidStrings({ "ENABLED", "DISABLED" })
    public UsageOfAction getUsageOfAction() {
        return usageOfAction;
    }

    public void setUsageOfAction(UsageOfAction usageOfAction) {
        this.usageOfAction = usageOfAction;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ClientSideAction clientSideAction) {
        getExemptUriRegularExpressions().clear();
        if (clientSideAction.exemptUriRegularExpressions() != null) {
            for (Regex r : clientSideAction.exemptUriRegularExpressions()) {
                RegexResource resource = newSubresource(RegexResource.class);
                resource.copyFrom(r);
                getExemptUriRegularExpressions().add(resource);
            }
        }

        setSensitivity(clientSideAction.sensitivity());
        setUsageOfAction(clientSideAction.usageOfAction());
    }

    ClientSideAction toClientSideAction() {
        ClientSideAction.Builder builder = ClientSideAction.builder()
            .sensitivity(getSensitivity())
            .usageOfAction(getUsageOfAction());

        if (!getExemptUriRegularExpressions().isEmpty()) {
            builder.exemptUriRegularExpressions(getExemptUriRegularExpressions().stream()
                .map(RegexResource::toRegex)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
