package gyro.aws.backup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;

public class BackupRule extends Diffable
    implements Copyable<software.amazon.awssdk.services.backup.model.BackupRule> {

    private Long completionWindowMinutes;
    private List<BackupCopyAction> copyAction;
    private Boolean enableContinuousBackup;
    private BackupLifecycle lifecycle;
    private Map<String, String> recoveryPointTags;
    private String name;
    private String schedule;
    private Long startWindowMinutes;
    private BackupVaultResource targetBackupVault;

    /**
     * The value in minutes after a backup job is successfully started before it must be completed or it will be canceled by AWS Backup.
     */
    public Long getCompletionWindowMinutes() {
        return completionWindowMinutes;
    }

    public void setCompletionWindowMinutes(Long completionWindowMinutes) {
        this.completionWindowMinutes = completionWindowMinutes;
    }

    /**
     * The array of copy actions, which contains the details of the copy operation.
     */
    public List<BackupCopyAction> getCopyAction() {
        if (copyAction == null) {
            copyAction = new ArrayList<>();
        }

        return copyAction;
    }

    public void setCopyAction(List<BackupCopyAction> copyAction) {
        this.copyAction = copyAction;
    }

    /**
     * When se to ``true``, AWS Backup creates continuous backups.
     */
    public Boolean getEnableContinuousBackup() {
        return enableContinuousBackup;
    }

    public void setEnableContinuousBackup(Boolean enableContinuousBackup) {
        this.enableContinuousBackup = enableContinuousBackup;
    }

    /**
     * The lifecycle which defines when a protected resource is transitioned to cold storage and when it expires.
     */
    public BackupLifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(BackupLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * The tags for the rule.
     */
    public Map<String, String> getRecoveryPointTags() {
        if (recoveryPointTags == null) {
            recoveryPointTags = new HashMap<>();
        }

        return recoveryPointTags;
    }

    public void setRecoveryPointTags(Map<String, String> recoveryPointTags) {
        this.recoveryPointTags = recoveryPointTags;
    }

    /**
     * The display name for the backup rule.
     */
    @Required
    @Regex("^[a-zA-Z0-9\\-\\_\\.]{1,50}$")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The CRON expression specifying when AWS Backup initiates a backup job.
     */
    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * The value in minutes after a backup is scheduled before a job will be canceled if it doesn't start successfully.
     */
    @Min(60)
    public Long getStartWindowMinutes() {
        return startWindowMinutes;
    }

    public void setStartWindowMinutes(Long startWindowMinutes) {
        this.startWindowMinutes = startWindowMinutes;
    }

    /**
     * The logical container where backups are stored.
     */
    @Required
    public BackupVaultResource getTargetBackupVault() {
        return targetBackupVault;
    }

    public void setTargetBackupVault(BackupVaultResource targetBackupVault) {
        this.targetBackupVault = targetBackupVault;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.backup.model.BackupRule model) {
        setCompletionWindowMinutes(model.completionWindowMinutes());
        setEnableContinuousBackup(model.enableContinuousBackup());
        setRecoveryPointTags(model.recoveryPointTags());
        setName(model.ruleName());
        setSchedule(model.scheduleExpression());
        setTargetBackupVault(findById(BackupVaultResource.class, model.targetBackupVaultName()));
        setStartWindowMinutes(model.startWindowMinutes());

        setLifecycle(null);
        if (model.lifecycle() != null) {
            BackupLifecycle backupLifecycle = newSubresource(BackupLifecycle.class);
            backupLifecycle.copyFrom(model.lifecycle());
            setLifecycle(backupLifecycle);
        }

        setCopyAction(null);
        if (model.hasCopyActions()) {
            setCopyAction(model.copyActions().stream().map(c -> {
                BackupCopyAction backupCopyAction = newSubresource(BackupCopyAction.class);
                backupCopyAction.copyFrom(c);
                return backupCopyAction;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return String.format("Rule name: %s", getName());
    }

    software.amazon.awssdk.services.backup.model.BackupRuleInput toBackupRuleInput() {
        return software.amazon.awssdk.services.backup.model.BackupRuleInput.builder()
            .completionWindowMinutes(getCompletionWindowMinutes())
            .enableContinuousBackup(getEnableContinuousBackup())
            .lifecycle(getLifecycle() != null ? getLifecycle().toLifecycle() : null)
            .recoveryPointTags(getRecoveryPointTags())
            .scheduleExpression(getSchedule())
            .ruleName(getName())
            .startWindowMinutes(getStartWindowMinutes())
            .targetBackupVaultName(getTargetBackupVault().getName())
            .copyActions(getCopyAction().stream().map(BackupCopyAction::toCopyAction).collect(Collectors.toList()))
            .build();
    }
}
