/*
 * Copyright 2026, Brightspot.
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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.eks.model.AddonIssue;

public class EksAddonIssue extends Diffable implements Copyable<AddonIssue> {

    private String code;
    private String message;
    private List<String> resourceIds;

    /**
     * A code that describes the type of issue.
     */
    @Output
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * A message that provides details about the issue and what might cause it.
     */
    @Output
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * The resource IDs of the issue.
     */
    @Output
    public List<String> getResourceIds() {
        if (resourceIds == null) {
            resourceIds = new ArrayList<>();
        }
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getResourceIds(), getCode());
    }

    @Override
    public void copyFrom(AddonIssue addonIssue) {
        setCode(addonIssue.codeAsString());
        setMessage(addonIssue.message());

        getResourceIds().clear();
        if (addonIssue.resourceIds() != null) {
            getResourceIds().addAll(addonIssue.resourceIds());
        }
    }
}
