package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.cloudfront.model.S3OriginConfig;

public class CloudFrontS3Origin extends Diffable implements Copyable<S3OriginConfig> {

    private String originAccessIdentity;

    /**
     * Origin access identity for serving private content through S3.
     */
    public String getOriginAccessIdentity() {
        if (originAccessIdentity == null) {
            return "";
        }

        return originAccessIdentity;
    }

    public void setOriginAccessIdentity(String originAccessIdentity) {
        this.originAccessIdentity = originAccessIdentity;
    }

    @Override
    public void copyFrom(S3OriginConfig s3OriginConfig) {
        setOriginAccessIdentity(s3OriginConfig.originAccessIdentity());
    }

    @Override
    public String primaryKey() {
        return "s3-origin";
    }

    @Override
    public String toDisplayString() {
        return "s3 origin";
    }

    S3OriginConfig toS3OriginConfig() {
        return S3OriginConfig.builder()
            .originAccessIdentity(getOriginAccessIdentity())
            .build();
    }
}
