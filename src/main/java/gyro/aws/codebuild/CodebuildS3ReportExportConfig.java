package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.S3ReportExportConfig;

public class CodebuildS3ReportExportConfig extends Diffable implements Copyable<S3ReportExportConfig> {

    private String bucket;
    private Boolean encryptionDisabled;
    private String encryptionKey;
    private String packaging;
    private String path;

    @Updatable
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    @Updatable
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Updatable
    @ValidStrings({ "ZIP", "NONE" })
    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void copyFrom(S3ReportExportConfig model) {
        setBucket(model.bucket());
        setEncryptionDisabled(model.encryptionDisabled());
        setEncryptionKey(model.encryptionKey());
        setPackaging(model.packagingAsString());
        setPath(model.path());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public S3ReportExportConfig toS3ReportExportConfig() {
        return S3ReportExportConfig.builder()
            .bucket(getBucket())
            .encryptionDisabled(getEncryptionDisabled())
            .encryptionKey(getEncryptionKey())
            .packaging(getPackaging())
            .path(getPath())
            .build();
    }
}
