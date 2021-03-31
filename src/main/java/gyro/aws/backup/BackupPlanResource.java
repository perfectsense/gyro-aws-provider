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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.CreateBackupPlanResponse;
import software.amazon.awssdk.services.backup.model.GetBackupPlanResponse;
import software.amazon.awssdk.services.backup.model.ResourceNotFoundException;

/**
 * Creates a backup plan.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::backup-plan backup
 *         configuration
 *             name: my-rds-backups
 *
 *             rule
 *                 name: weekly
 *                 target-backup-vault: $(aws::backup-vault target-vault)
 *
 *                 schedule: 'cron(0 5 ? * * *)'
 *                 start-window-minutes: 60
 *
 *                 completion-window-minutes: 120
 *                 enable-continuous-backup: false
 *
 *                 recovery-point-tags: {
 *                    "example-tag": "example-value"
 *                 }
 *
 *                 lifecycle
 *                     delete-after-days: 365
 *                     move-to-cold-storage-after-days: 30
 *                 end
 *
 *                 copy-action
 *                     destination-backup-vault: $(aws::backup-vault destination-vault)
 *
 *                     lifecycle
 *                         delete-after-days: 365
 *                         move-to-cold-storage-after-days: 30
 *                     end
 *                 end
 *             end
 *         end
 *
 *         tags: {
 *             project: "my project"
 *         }
 *     end
 */
@Type("backup-plan")
public class BackupPlanResource extends AwsResource implements Copyable<GetBackupPlanResponse> {

    private BackupPlan configuration;
    private Map<String, String> tags;
    private String creatorRequestId;

    // Read=only
    private String arn;
    private String id;

    /**
     * The body of a backup plan.
     */
    @Required
    @Updatable
    public BackupPlan getConfiguration() {
        return configuration;
    }

    public void setConfiguration(BackupPlan configuration) {
        this.configuration = configuration;
    }

    /**
     * The tags for the plan.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ID Identifies the request and allows failed requests to be retried without the risk of running the operation twice.
     */
    public String getCreatorRequestId() {
        return creatorRequestId;
    }

    public void setCreatorRequestId(String creatorRequestId) {
        this.creatorRequestId = creatorRequestId;
    }

    /**
     * The ARN of the backup plan.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ID of the backup plan.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(GetBackupPlanResponse model) {
        setCreatorRequestId(model.creatorRequestId());
        setArn(model.backupPlanArn());
        setId(model.backupPlanId());

        BackupPlan plan = newSubresource(BackupPlan.class);
        plan.copyFrom(model.backupPlan());
        setConfiguration(plan);

        BackupClient client = createClient(BackupClient.class);
        setTags(client.listTags(r -> r.resourceArn(getArn())).tags());
    }

    @Override
    public boolean refresh() {
        BackupClient client = createClient(BackupClient.class);

        try {
            GetBackupPlanResponse backupPlanResponse = client.getBackupPlan(r -> r.backupPlanId(getId()));

            copyFrom(backupPlanResponse);

            return true;

        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        CreateBackupPlanResponse response = client.createBackupPlan(r -> r.backupPlan(getConfiguration().toBackupPlanInput())
            .backupPlanTags(getTags())
            .creatorRequestId(getCreatorRequestId()));

        setId(response.backupPlanId());
        setArn(response.backupPlanArn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        if (changedFieldNames.contains("tags")) {
            BackupPlanResource resource = (BackupPlanResource) current;
            client.untagResource(r -> r.resourceArn(getArn()).tagKeyList(resource.getTags().keySet()));
            client.tagResource(r -> r.resourceArn(getArn()).tags(getTags()));
        }

        client.updateBackupPlan(r -> r.backupPlanId(getId()).backupPlan(getConfiguration().toBackupPlanInput()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        client.deleteBackupPlan(r -> r.backupPlanId(getId()));
    }
}
