package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.SourceSelectionCriteria;
import software.amazon.awssdk.services.s3.model.SseKmsEncryptedObjectsStatus;

public class S3SourceSelectionCriteria extends Diffable implements Copyable<SourceSelectionCriteria> {
    private SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus;

    /**
     * Status of Server Side Encryption. Valid values are ``ENABLED`` or ``DISABLED``
     */
    @Updatable
    public SseKmsEncryptedObjectsStatus getSseKmsEncryptedObjectsStatus() {
        if(sseKmsEncryptedObjectsStatus == null){
            this.sseKmsEncryptedObjectsStatus = SseKmsEncryptedObjectsStatus.ENABLED;
        }
        return sseKmsEncryptedObjectsStatus;
    }

    public void setSseKmsEncryptedObjectsStatus(SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus) {
        this.sseKmsEncryptedObjectsStatus = sseKmsEncryptedObjectsStatus;
    }

    @Override
    public String primaryKey() {
        return "source selection criteria";
    }

    @Override
    public void copyFrom(SourceSelectionCriteria sourceSelectionCriteria) {
        setSseKmsEncryptedObjectsStatus(sourceSelectionCriteria.sseKmsEncryptedObjects().status());
    }

    SourceSelectionCriteria toSourceSelectionCriteria(){
        return SourceSelectionCriteria.builder()
                .sseKmsEncryptedObjects(
                        k -> k.status(getSseKmsEncryptedObjectsStatus())
                ).build();
    }
}
