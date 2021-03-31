package gyro.aws.backup;

import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.CreateBackupSelectionResponse;
import software.amazon.awssdk.services.backup.model.GetBackupSelectionResponse;

/**
 * Creates backup selection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::backup-selection production
 *         backup-plan: $(aws::backup-plan backup)
 *
 *         configuration
 *             name: production
 *             role: $(external-query aws::iam-role { name: 'AWSBackupDefaultServiceRole'})
 *             resources: [$(aws::db-cluster db-cluster-example).arn]
 *         end
 *     end
 */
@Type("backup-selection")
public class BackupSelectionResource extends AwsResource implements Copyable<GetBackupSelectionResponse> {

    private BackupPlanResource backupPlan;
    private BackupSelection configuration;
    private String creatorRequestId;

    // Read-only
    private String id;

    /**
     * Tthe backup plan to be associated with the selection of resources.
     */
    @Required
    public BackupPlanResource getBackupPlan() {
        return backupPlan;
    }

    public void setBackupPlan(BackupPlanResource backupPlan) {
        this.backupPlan = backupPlan;
    }

    /**
     * The body of a request to assign a set of resources to a backup plan.
     */
    @Required
    public BackupSelection getConfiguration() {
        return configuration;
    }

    public void setConfiguration(BackupSelection configuration) {
        this.configuration = configuration;
    }

    /**
     * The ID that identifies the request and allows failed requests to be retried without the risk of running the operation twice.
     */
    public String getCreatorRequestId() {
        return creatorRequestId;
    }

    public void setCreatorRequestId(String creatorRequestId) {
        this.creatorRequestId = creatorRequestId;
    }

    /**
     * The ID of the backup selection.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(GetBackupSelectionResponse model) {
        setBackupPlan(findById(BackupPlanResource.class, model.backupPlanId()));
        setCreatorRequestId(model.creatorRequestId());
        setId(model.selectionId());

        BackupSelection selection = newSubresource(BackupSelection.class);
        selection.copyFrom(model.backupSelection());
        setConfiguration(selection);
    }

    @Override
    public boolean refresh() {
        BackupClient client = createClient(BackupClient.class);

        GetBackupSelectionResponse selection = client.getBackupSelection(r -> r.backupPlanId(getBackupPlan().getId())
            .selectionId(getId()));

        if (selection == null) {
            return false;
        }

        copyFrom(selection);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        CreateBackupSelectionResponse response = client.createBackupSelection(r -> r.backupPlanId(getBackupPlan().getId())
            .backupSelection(getConfiguration().toBackupSelection())
            .creatorRequestId(getCreatorRequestId()));

        setId(response.selectionId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        BackupClient client = createClient(BackupClient.class);

        client.deleteBackupSelection(r -> r.backupPlanId(getBackupPlan().getId()).selectionId(getId()));
    }
}
