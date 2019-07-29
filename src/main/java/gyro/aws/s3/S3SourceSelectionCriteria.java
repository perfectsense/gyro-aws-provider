package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.SourceSelectionCriteria;
import software.amazon.awssdk.services.s3.model.SseKmsEncryptedObjectsStatus;

public class S3SourceSelectionCriteria extends Diffable implements Copyable<SourceSelectionCriteria> {
    private SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus;

    /**
     * Status of Server Side Encryption. Valid values are ``ENABLED`` and ``DISABLED`` (Required)
     */
    @Updatable
    public SseKmsEncryptedObjectsStatus getSseKmsEncryptedObjectsStatus() {
        return sseKmsEncryptedObjectsStatus;
    }

    public void setSseKmsEncryptedObjectsStatus(SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus) {
        this.sseKmsEncryptedObjectsStatus = sseKmsEncryptedObjectsStatus;
    }

    @Override
    public void copyFrom(SourceSelectionCriteria sourceSelectionCriteria) {
        if(sourceSelectionCriteria.sseKmsEncryptedObjects() != null){
            setSseKmsEncryptedObjectsStatus(sourceSelectionCriteria.sseKmsEncryptedObjects().status());
        }
    }

    @Override
    public String primaryKey() {
        return "source selection criteria";
    }

    SourceSelectionCriteria toSourceSelectionCriteria(){
        SourceSelectionCriteria.Builder builder = SourceSelectionCriteria.builder();

        if(getSseKmsEncryptedObjectsStatus() != null){
            builder.sseKmsEncryptedObjects(
                k -> k.status(getSseKmsEncryptedObjectsStatus())
            );
        }

        return builder.build();
    }
}
