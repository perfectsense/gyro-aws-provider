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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.ClientSideAction;
import software.amazon.awssdk.services.wafv2.model.Regex;

public class ClientSideActionResource extends Diffable implements Copyable<ClientSideAction> {

    private List<RegexResource> exemptUriRegularExpressions;
    private String sensitivity;
    private String usageOfAction;

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
    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Usage configuration for the client-side action, for example ENABLED or DISABLED.
     */
    @Required
    @Updatable
    @ValidStrings({ "ENABLED", "DISABLED" })
    public String getUsageOfAction() {
        return usageOfAction;
    }

    public void setUsageOfAction(String usageOfAction) {
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

        setSensitivity(clientSideAction.sensitivityAsString());
        setUsageOfAction(clientSideAction.usageOfActionAsString());
    }

    ClientSideAction toClientSideAction() {
        ClientSideAction.Builder builder = ClientSideAction.builder();

        if (!getExemptUriRegularExpressions().isEmpty()) {
            List<Regex> regexList = new ArrayList<>();
            for (RegexResource r : getExemptUriRegularExpressions()) {
                regexList.add(r.toRegex());
            }
            builder.exemptUriRegularExpressions(regexList);
        }

        if (getSensitivity() != null) {
            builder.sensitivity(getSensitivity());
        }

        if (getUsageOfAction() != null) {
            builder.usageOfAction(getUsageOfAction());
        }

        return builder.build();
    }
}
