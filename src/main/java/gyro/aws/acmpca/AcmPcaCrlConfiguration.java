package gyro.aws.acmpca;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.acmpca.model.CrlConfiguration;

public class AcmPcaCrlConfiguration extends Diffable implements Copyable<CrlConfiguration> {
    private String customName;
    private Boolean enabled;
    private Integer expirationDays;
    private BucketResource bucket;

    /**
     * Name inserted into the certificate CRL Distribution Points extension that enables the use of an alias for the CRL distribution point.
     */
    @Updatable
    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    /**
     * Enable or Disable this CRL. Defaults to Disabled.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Number of days until a certificate expires.
     */
    @Updatable
    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }

    /**
     * The S3 bucket that contains the CRL
     */
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    @Override
    public void copyFrom(CrlConfiguration crlConfiguration) {
        setCustomName(crlConfiguration.customCname());
        setEnabled(crlConfiguration.enabled());
        setExpirationDays(crlConfiguration.expirationInDays());
        setBucket(!ObjectUtils.isBlank(crlConfiguration.s3BucketName()) ? findById(BucketResource.class, crlConfiguration.s3BucketName()) : null);
    }

    @Override
    public String primaryKey() {
        return "crl configuration";
    }

    CrlConfiguration toCrlConfiguration() {
        return CrlConfiguration.builder()
            .customCname(getCustomName())
            .enabled(getEnabled())
            .expirationInDays(getExpirationDays())
            .s3BucketName(getBucket() != null ? getBucket().getName() : null)
            .build();
    }
}
