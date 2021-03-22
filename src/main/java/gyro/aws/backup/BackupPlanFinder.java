package gyro.aws.backup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.BackupException;
import software.amazon.awssdk.services.backup.model.BackupPlansListMember;
import software.amazon.awssdk.services.backup.model.GetBackupPlanResponse;

/**
 * Query Backup Plan.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    backup-plan: $(external-query aws::backup-plan { id: '93b10784-b8d2-4f82-9ba9-b8203744f7b9'})
 */
@Type("backup-plan")
public class BackupPlanFinder extends AwsFinder<BackupClient, GetBackupPlanResponse, BackupPlanResource> {

    private String id;

    /**
     * The ID of the backup plan.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<GetBackupPlanResponse> findAllAws(BackupClient client) {
        List<String> ids = client.listBackupPlans().backupPlansList().stream()
            .map(BackupPlansListMember::backupPlanId).collect(Collectors.toList());

        return ids.stream().map(i -> client.getBackupPlan(r -> r.backupPlanId(i))).collect(Collectors.toList());
    }

    @Override
    protected List<GetBackupPlanResponse> findAws(BackupClient client, Map<String, String> filters) {
        List<GetBackupPlanResponse> responses = new ArrayList<>();

        try {
            responses = Collections.singletonList(client.getBackupPlan(r -> r.backupPlanId(filters.get("id"))));
        } catch (BackupException ex) {
            // ignore
        }

        return responses;
    }
}
