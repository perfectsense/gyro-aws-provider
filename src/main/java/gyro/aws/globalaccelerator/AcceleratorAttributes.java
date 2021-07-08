package gyro.aws.globalaccelerator;

import gyro.core.resource.Diffable;

public class AcceleratorAttributes extends Diffable {

    private Boolean flowLogsEnabled;
    private String flowLogsS3Bucket;
    private String flowLogsS3Prefix;

    @Override
    public String primaryKey() {
        return "";
    }

    /**
     * Whether flow logs are enabled.
     */
    public Boolean getFlowLogsEnabled() {
        return flowLogsEnabled;
    }

    public void setFlowLogsEnabled(Boolean flowLogsEnabled) {
        this.flowLogsEnabled = flowLogsEnabled;
    }

    /**
     * The bucket to upload flow logs to.
     */
    public String getFlowLogsS3Bucket() {
        return flowLogsS3Bucket;
    }

    public void setFlowLogsS3Bucket(String flowLogsS3Bucket) {
        this.flowLogsS3Bucket = flowLogsS3Bucket;
    }

    /**
     * The location to upload flow logs in the bucket.
     */
    public String getFlowLogsS3Prefix() {
        return flowLogsS3Prefix;
    }

    public void setFlowLogsS3Prefix(String flowLogsS3Prefix) {
        this.flowLogsS3Prefix = flowLogsS3Prefix;
    }
}
