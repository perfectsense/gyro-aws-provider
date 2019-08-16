package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.s3.model.AccessControlTranslation;
import software.amazon.awssdk.services.s3.model.OwnerOverride;

public class S3AccessControlTranslation extends Diffable implements Copyable<AccessControlTranslation> {


    private OwnerOverride ownerOverride;

    /**
     * Sets the ownership of the replica. Valid value is ``DESTINATION``
     */
    public OwnerOverride getOwnerOverride() {
        return ownerOverride;
    }

    public void setOwnerOverride(OwnerOverride ownerOverride) {
        this.ownerOverride = ownerOverride;
    }

    @Override
    public String primaryKey() {
        return "access control translation";
    }

    @Override
    public void copyFrom(AccessControlTranslation accessControlTranslation) {
        setOwnerOverride(accessControlTranslation.owner());
    }

    AccessControlTranslation toAccessControlTranslation() {
        return AccessControlTranslation.builder()
                .owner(getOwnerOverride())
                .build();
    }

}
