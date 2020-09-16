package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.S3LogsConfig;

public class CodebuildS3LogsConfig extends Diffable implements Copyable<S3LogsConfig> {

    private String status;
    private Boolean encryptionDisabled;
    private String location;

    @Updatable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void copyFrom(S3LogsConfig model) {
        setStatus(model.statusAsString());
        setEncryptionDisabled(model.encryptionDisabled());
        setLocation(model.location());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
