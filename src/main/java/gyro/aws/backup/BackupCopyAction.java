package gyro.aws.backup;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.backup.model.CopyAction;

public class BackupCopyAction extends Diffable implements Copyable<CopyAction> {

    private BackupVaultResource destinationBackupVault;
    private BackupLifecycle lifecycle;

    /**
     * The destination backup vault for the copied backup.
     */
    @Required
    public BackupVaultResource getDestinationBackupVault() {
        return destinationBackupVault;
    }

    public void setDestinationBackupVault(BackupVaultResource destinationBackupVault) {
        this.destinationBackupVault = destinationBackupVault;
    }

    /**
     * The value of the Lifecycle property for this object.
     */
    public BackupLifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(BackupLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public void copyFrom(CopyAction model) {
        setDestinationBackupVault(
            findById(BackupVaultResource.class, getVaultNameFromArn(model.destinationBackupVaultArn())));

        setLifecycle(null);
        if (model.lifecycle() != null) {
            BackupLifecycle backupLifecycle = newSubresource(BackupLifecycle.class);
            backupLifecycle.copyFrom(model.lifecycle());
            setLifecycle(backupLifecycle);
        }
    }

    @Override
    public String primaryKey() {
        return String.format("Vault: %s, Lifecycle: (%s,  %s)", getDestinationBackupVault().getName(),
            getLifecycle().getDeleteAfterDays(), getLifecycle().getMoveToColdStorageAfterDays());
    }

    CopyAction toCopyAction() {
        return CopyAction.builder().destinationBackupVaultArn(getDestinationBackupVault().getArnFromName())
            .lifecycle(getLifecycle().toLifecycle()).build();
    }

    private String getVaultNameFromArn(String arn) {
        return arn.split("backup-vault:")[1];
    }
}
