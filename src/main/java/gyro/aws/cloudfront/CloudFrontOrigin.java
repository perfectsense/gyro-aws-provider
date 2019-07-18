package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.CustomHeaders;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.OriginCustomHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloudFrontOrigin extends Diffable implements Copyable<Origin> {

    private String id;
    private String domainName;
    private String originPath;
    private Map<String, String> customHeaders;
    private CloudFrontS3Origin s3Origin;
    private CloudFrontCustomOrigin customOrigin;

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
    @Updatable
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Optional path to request content from a specific directory of the origin.
     */
    @Updatable
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
    @Updatable
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
    @Updatable
    public CloudFrontS3Origin getS3Origin() {
        if (s3Origin == null && customOrigin == null) {
            return newSubresource(CloudFrontS3Origin.class);
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
    @Updatable
    public CloudFrontCustomOrigin getCustomOrigin() {
        return customOrigin;
    }

    public void setCustomOrigin(CloudFrontCustomOrigin customOrigin) {
        this.customOrigin = customOrigin;
    }

    @Override
    public void copyFrom(Origin origin) {
        setId(origin.id());
        setDomainName(origin.domainName());
        setOriginPath(origin.originPath());

        if (origin.customHeaders().quantity() > 0) {
            for (OriginCustomHeader header : origin.customHeaders().items()) {
                getCustomHeaders().put(header.headerName(), header.headerValue());
            }
        }

        if (origin.customOriginConfig() != null) {
            CloudFrontCustomOrigin cloudFrontCustomOrigin = newSubresource(CloudFrontCustomOrigin.class);
            cloudFrontCustomOrigin.copyFrom(origin.customOriginConfig());
            setCustomOrigin(cloudFrontCustomOrigin);
        }

        if (origin.s3OriginConfig() != null) {
            CloudFrontS3Origin cloudFrontS3Origin = newSubresource(CloudFrontS3Origin.class);
            cloudFrontS3Origin.copyFrom(origin.s3OriginConfig());
            setS3Origin(cloudFrontS3Origin);
        }
    }

    @Override
    public String primaryKey() {
        return getId();
    }

    Origin toOrigin() {
        List<OriginCustomHeader> headers = getCustomHeaders().entrySet()
            .stream()
            .map(e -> OriginCustomHeader.builder().headerName(e.getKey()).headerValue(e.getValue()).build())
            .collect(Collectors.toList());

        CustomHeaders customHeaders = CustomHeaders.builder()
            .items(headers)
            .quantity(headers.size())
            .build();

        if (getCustomOrigin() == null && getS3Origin() == null) {
            setS3Origin(newSubresource(CloudFrontS3Origin.class));
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
}
