package gyro.aws.rds;

import gyro.aws.iam.RoleResource;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class DbClusterS3Import extends Diffable {

    private String sourceEngine;
    private String sourceEngineVersion;
    private BucketResource s3Bucket;
    private String s3Prefix;
    private RoleResource s3IngestionRole;

    /**
     * The identifier for the database engine that was backed up to create the files stored in the Amazon S3 bucket.
     */
    @ValidStrings("mysql")
    public String getSourceEngine() {
        return sourceEngine;
    }

    public void setSourceEngine(String sourceEngine) {
        this.sourceEngine = sourceEngine;
    }

    /**
     * The version of the database that the backup files were created from.
     */
    public String getSourceEngineVersion() {
        return sourceEngineVersion;
    }

    public void setSourceEngineVersion(String sourceEngineVersion) {
        this.sourceEngineVersion = sourceEngineVersion;
    }

    /**
     * The Amazon S3 bucket that contains the data used to create the Amazon Aurora DB cluster.
     */
    @Required
    public BucketResource getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(BucketResource s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The prefix for all of the file names that contain the data used to create the Amazon Aurora DB cluster.
     */
    public String getS3Prefix() {
        return s3Prefix;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }

    /**
     * The IAM role that authorizes Amazon RDS to access the Amazon S3 bucket on your behalf.
     */
    @Required
    public RoleResource getS3IngestionRole() {
        return s3IngestionRole;
    }

    public void setS3IngestionRole(RoleResource s3IngestionRole) {
        this.s3IngestionRole = s3IngestionRole;
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
