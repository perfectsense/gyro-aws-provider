package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.ViewerCertificate;

public class CloudFrontViewerCertificate extends Diffable implements Copyable<ViewerCertificate> {

    private Boolean cloudfrontDefaultCertificate;
    private String acmCertificateArn;
    private String iamCertificateId;
    private String minimumProtocolVersion;
    private String sslSupportMethod;

    /**
     * Use the default CloudFront SSL certificate (i.e. ``*.cloudfront.net``).
     */
    @Updatable
    public Boolean getCloudfrontDefaultCertificate() {
        if (cloudfrontDefaultCertificate == null) {
            cloudfrontDefaultCertificate = true;
        }

        return cloudfrontDefaultCertificate;
    }

    public void setCloudfrontDefaultCertificate(Boolean cloudfrontDefaultCertificate) {
        this.cloudfrontDefaultCertificate = cloudfrontDefaultCertificate;
    }

    /**
     * ARN for an ACM generated certificate.
     */
    @Updatable
    public String getAcmCertificateArn() {
        return acmCertificateArn;
    }

    public void setAcmCertificateArn(String acmCertificateArn) {
        this.acmCertificateArn = acmCertificateArn;
    }

    /**
     * ID for certificated uploaded to IAM.
     */
    @Updatable
    public String getIamCertificateId() {
        return iamCertificateId;
    }

    public void setIamCertificateId(String iamCertificateId) {
        this.iamCertificateId = iamCertificateId;
    }

    /**
     * Minimum SSL protocol. Valid valies are ``SSLv3``, ``TLSv1``, ``TLSv1_2016``, ``TLSv1.1_2016``, ``TLSv1.2_2018``.
     */
    @Updatable
    public String getMinimumProtocolVersion() {
        if (minimumProtocolVersion == null) {
            minimumProtocolVersion = "TLSv1";
        }

        return minimumProtocolVersion;
    }

    public void setMinimumProtocolVersion(String minimumProtocolVersion) {
        this.minimumProtocolVersion = minimumProtocolVersion;
    }

    /**
     * Whether CloudFront uses a dedicated IP or SNI for serving SSL traffic. Valid values are ``vip`` or ``sni-only``. There is a significant additional monthly charge for ``vip`.
     */
    @Updatable
    public String getSslSupportMethod() {
        if (getCloudfrontDefaultCertificate())  {
            return null;
        } else if (sslSupportMethod == null) {
            return "sni-only";
        }

        return sslSupportMethod;
    }

    public void setSslSupportMethod(String sslSupportMethod) {
        this.sslSupportMethod = sslSupportMethod;
    }

    @Override
    public void copyFrom(ViewerCertificate viewerCertificate) {
        setCloudfrontDefaultCertificate(viewerCertificate.cloudFrontDefaultCertificate());
        setAcmCertificateArn(viewerCertificate.acmCertificateArn());
        setIamCertificateId(viewerCertificate.iamCertificateId());
        setMinimumProtocolVersion(viewerCertificate.minimumProtocolVersionAsString());
        setSslSupportMethod(viewerCertificate.sslSupportMethodAsString());
    }

    @Override
    public String primaryKey() {
        return "viewer-certificate";
    }

    ViewerCertificate toViewerCertificate() {
        return ViewerCertificate.builder()
            .acmCertificateArn(getAcmCertificateArn())
            .iamCertificateId(getIamCertificateId())
            .minimumProtocolVersion(getMinimumProtocolVersion())
            .sslSupportMethod(getSslSupportMethod())
            .cloudFrontDefaultCertificate(getCloudfrontDefaultCertificate())
            .build();
    }
}
