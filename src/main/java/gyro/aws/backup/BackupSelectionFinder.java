package gyro.aws.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.BackupException;
import software.amazon.awssdk.services.backup.model.BackupPlansListMember;
import software.amazon.awssdk.services.backup.model.GetBackupSelectionResponse;

/**
 * Query Backup Selection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    backup-selection: $(external-query aws::backup-selection { id: '2adb3482-3b15-479e-befd-8bc20c95e655', backup-plan-id: '93b10784-b8d2-4f82-9ba9-b8203744f7b9' })
 */
@Type("backup-selection")
public class BackupSelectionFinder
    extends AwsFinder<BackupClient, GetBackupSelectionResponse, BackupSelectionResource> {

    private String backupPlanId;
    private String id;

    /**
     * The ID of the backup plan.
     */
    public String getBackupPlanId() {
        return backupPlanId;
    }

    public void setBackupPlanId(String backupPlanId) {
        this.backupPlanId = backupPlanId;
    }

    /**
     * The ID of the backup selection.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<GetBackupSelectionResponse> findAllAws(BackupClient client) {
        List<GetBackupSelectionResponse> selections = new ArrayList<>();

        List<String> backupPlanIds = client.listBackupPlans()
            .backupPlansList()
            .stream()
            .map(BackupPlansListMember::backupPlanId)
            .collect(Collectors.toList());

        backupPlanIds.forEach(i -> selections.addAll(client.listBackupSelections(r -> r.backupPlanId(i))
            .backupSelectionsList().stream()
            .map(s -> client.getBackupSelection(r -> r.selectionId(s.selectionId()).backupPlanId(i)))
            .collect(Collectors.toList())));

        return selections;
    }

    @Override
    protected List<GetBackupSelectionResponse> findAws(BackupClient client, Map<String, String> filters) {
        List<GetBackupSelectionResponse> selections = new ArrayList<>();
        List<String> backupPlanIds = new ArrayList<>();

        if (filters.containsKey("backup-plan-id")) {
            backupPlanIds.add(filters.get("backup-plan-id"));

        } else {
            backupPlanIds.addAll(client.listBackupPlans()
                .backupPlansList()
                .stream()
                .map(BackupPlansListMember::backupPlanId)
                .collect(Collectors.toList()));
        }

        if (filters.containsKey("id")) {
            backupPlanIds.forEach(i -> {
                try {
                    GetBackupSelectionResponse backupSelection = client.getBackupSelection(r -> r.backupPlanId(i)
                        .selectionId(filters.get("id")));

                    if (backupSelection != null) {
                        selections.add(backupSelection);
                    }
                } catch (BackupException ex) {
                    // ignore
                }
            });

        } else {
            backupPlanIds.forEach(i -> {
                try {
                    selections.addAll(client.listBackupSelections(r -> r.backupPlanId(i))
                        .backupSelectionsList().stream()
                        .map(s -> client.getBackupSelection(r -> r.selectionId(s.selectionId()).backupPlanId(i)))
                        .collect(Collectors.toList()));
                } catch (BackupException ex) {
                    // ignore
                }
            });
        }

        return selections;
    }
}
