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
import software.amazon.awssdk.services.eks.model.AddonHealth;
import software.amazon.awssdk.services.eks.model.AddonIssue;

public class EksAddonHealth extends Diffable implements Copyable<AddonHealth> {

    private List<EksAddonIssue> issues;

    /**
     * Health issues for the add-on.
     */
    @Output
    public List<EksAddonIssue> getIssues() {
        if (issues == null) {
            issues = new ArrayList<>();
        }
        return issues;
    }

    public void setIssues(List<EksAddonIssue> issues) {
        this.issues = issues;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AddonHealth addonHealth) {
        getIssues().clear();
        if (addonHealth.issues() != null) {
            for (AddonIssue issue : addonHealth.issues()) {
                EksAddonIssue i = newSubresource(EksAddonIssue.class);
                i.copyFrom(issue);
                getIssues().add(i);
            }
        }
    }
}
