/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class BackupSelection extends Diffable
    implements Copyable<software.amazon.awssdk.services.backup.model.BackupSelection> {

    private RoleResource role;
    private List<BackupCondition> condition;
    private List<String> resources;
    private String name;

    /**
     * The IAM role that AWS Backup uses to authenticate when backing up the target resource.
     */
    @Required
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The list of conditions used to specify a set of resources to assign to a backup plan.
     *
     * @subresource gyro.aws.backup.BackupCondition
     */
    public List<BackupCondition> getCondition() {
        if (condition == null) {
            condition = new ArrayList<>();
        }

        return condition;
    }

    public void setCondition(List<BackupCondition> condition) {
        this.condition = condition;
    }

    /**
     * The list of strings that contain ARNs of resources to assign to a backup plan.
     */
    public List<String> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
        }

        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    /**
     * The display name of a resource selection document.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.backup.model.BackupSelection model) {
        setRole(findById(RoleResource.class, model.iamRoleArn()));
        setName(model.selectionName());
        setResources(model.resources());

        setCondition(null);
        if (model.hasListOfTags()) {
            setCondition(model.listOfTags().stream().map(t -> {
                BackupCondition backupCondition = newSubresource(BackupCondition.class);
                backupCondition.copyFrom(t);
                return backupCondition;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    software.amazon.awssdk.services.backup.model.BackupSelection toBackupSelection() {
        return software.amazon.awssdk.services.backup.model.BackupSelection.builder()
            .iamRoleArn(getRole().getArn())
            .listOfTags(getCondition().stream().map(BackupCondition::toCondition).collect(Collectors.toList()))
            .resources(getResources())
            .selectionName(getName())
            .build();
    }
}
