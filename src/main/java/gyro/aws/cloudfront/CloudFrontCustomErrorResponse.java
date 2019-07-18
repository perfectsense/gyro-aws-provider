package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.cloudfront.model.CustomErrorResponse;

public class CloudFrontCustomErrorResponse extends Diffable implements Copyable<CustomErrorResponse> {

    private long ttl;
    private Integer errorCode;
    private String responseCode;
    private String responsePagePath;
    private Boolean customizeErrorResponse;

    /**
     * The minimum amount of time to cache this error code.
     */
    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * HTTP error code to return a custom response for.
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Custom HTTP status code to return.
     */
    public String getResponseCode() {
        if (responseCode == null) {
            return "";
        }

        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Path to a custom error page.
     */
    public String getResponsePagePath() {
        if (responsePagePath == null) {
            responsePagePath = "";
        }

        return responsePagePath;
    }

    public void setResponsePagePath(String responsePagePath) {
        this.responsePagePath = responsePagePath;
    }

    public Boolean getCustomizeErrorResponse() {
        if (customizeErrorResponse == null) {
            customizeErrorResponse = false;
        }

        return customizeErrorResponse;
    }

    public void setCustomizeErrorResponse(Boolean customizeErrorResponse) {
        this.customizeErrorResponse = customizeErrorResponse;
    }

    @Override
    public void copyFrom(CustomErrorResponse errorResponse) {
        setTtl(errorResponse.errorCachingMinTTL());
        setErrorCode(errorResponse.errorCode());
        setResponseCode(errorResponse.responseCode());
        setResponsePagePath(errorResponse.responsePagePath());
    }

    @Override
    public String primaryKey() {
        return getErrorCode() != null ? getErrorCode().toString() : "";
    }

    CustomErrorResponse toCustomErrorResponse() {
        return CustomErrorResponse.builder()
            .errorCachingMinTTL(getTtl())
            .errorCode(getErrorCode())
            .responseCode(getResponseCode())
            .responsePagePath(getResponsePagePath()).build();
    }
}
