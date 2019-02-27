package gyro.aws.cloudfront;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;
import software.amazon.awssdk.services.cloudfront.model.CustomHeaders;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.OriginCustomHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloudFrontOrigin extends Diffable {

    private String id;
    private String domainName;
    private String originPath;
    private Map<String, String> customHeaders;
    private CloudFrontS3Origin s3Origin;
    private CloudFrontCustomOrigin customOrigin;

    public CloudFrontOrigin() {
    }

    public CloudFrontOrigin(Origin origin) {
        setId(origin.id());
        setDomainName(origin.domainName());
        setOriginPath(origin.originPath());

        if (origin.customHeaders().quantity() > 0) {
            for (OriginCustomHeader header : origin.customHeaders().items()) {
                getCustomHeaders().put(header.headerName(), header.headerValue());
            }
        }

        if (origin.customOriginConfig() != null) {
            setCustomOrigin(new CloudFrontCustomOrigin(origin.customOriginConfig()));
        }

        if (origin.s3OriginConfig() != null) {
            setS3Origin(new CloudFrontS3Origin(origin.s3OriginConfig()));
        }
    }

    /**
     * A unique ID for this origin.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The DNS name of the origin.
     */
    @ResourceDiffProperty(updatable = true)
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Optional path to request content from a specific directory of the origin.
     */
    @ResourceDiffProperty(updatable = true)
    public String getOriginPath() {
        if (originPath == null) {
            return "";
        }

        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    /**
     * A map of custom headers to send the origin on every request.
     */
    @ResourceDiffProperty(updatable = true)
    public Map<String, String> getCustomHeaders() {
        if (customHeaders == null) {
            return new HashMap<>();
        }

        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    /**
     * S3 configuration for this origin.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontS3Origin
     */
    @ResourceDiffProperty(updatable = true)
    public CloudFrontS3Origin getS3Origin() {
        if (s3Origin == null && customOrigin == null) {
            return new CloudFrontS3Origin();
        }

        return s3Origin;
    }

    public void setS3Origin(CloudFrontS3Origin s3Origin) {
        this.s3Origin = s3Origin;
    }

    /**
     * Custom configuration for this origin.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontCustomOrigin
     */
    @ResourceDiffProperty(updatable = true)
    public CloudFrontCustomOrigin getCustomOrigin() {
        return customOrigin;
    }

    public void setCustomOrigin(CloudFrontCustomOrigin customOrigin) {
        this.customOrigin = customOrigin;
    }

    public Origin toOrigin() {
        List<OriginCustomHeader> headers = getCustomHeaders().entrySet()
            .stream()
            .map(e -> OriginCustomHeader.builder().headerName(e.getKey()).headerValue(e.getValue()).build())
            .collect(Collectors.toList());

        CustomHeaders customHeaders = CustomHeaders.builder()
            .items(headers)
            .quantity(headers.size())
            .build();

        if (getCustomOrigin() == null && getS3Origin() == null) {
            setS3Origin(new CloudFrontS3Origin());
        }

        return Origin.builder()
            .id(getId())
            .domainName(getDomainName())
            .originPath(getOriginPath())
            .customHeaders(customHeaders)
            .s3OriginConfig(getS3Origin() != null ? getS3Origin().toS3OriginConfig() : null)
            .customOriginConfig(getCustomOrigin() != null ? getCustomOrigin().toCustomOriginConfig() : null)
            .build();
    }

    @Override
    public String primaryKey() {
        return getId();
    }

    @Override
    public String toDisplayString() {
        return "origin - targetId: " + getId();
    }
}
