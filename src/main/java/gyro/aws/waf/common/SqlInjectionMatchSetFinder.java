package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;

abstract public class SqlInjectionMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, SqlInjectionMatchSet, U> {
    private String SqlInjectionMatchSetId;

    /**
     * The ID of sql injection match set.
     */
    public String getSqlInjectionMatchSetId() {
        return SqlInjectionMatchSetId;
    }

    public void setSqlInjectionMatchSetId(String sqlInjectionMatchSetId) {
        SqlInjectionMatchSetId = sqlInjectionMatchSetId;
    }
}
