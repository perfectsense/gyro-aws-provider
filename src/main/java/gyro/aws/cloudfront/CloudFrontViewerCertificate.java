/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
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
            cloudfrontDefaultCertificate = acmCertificateArn == null && iamCertificateId == null;
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
     * Minimum SSL protocol.
     */
    @Updatable
    @ValidStrings({"SSLv3", "TLSv1", "TLSv1_2016", "TLSv1.1_2016", "TLSv1.2_2018", "TLSv1.2_2019", "TLSv1.2_2021"})
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
     * Whether CloudFront uses a dedicated IP or SNI for serving SSL traffic. There is a significant additional monthly charge for ``vip`.
     */
    @Updatable
    @ValidStrings({"vip", "sni-only"})
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
