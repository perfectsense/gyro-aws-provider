package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.Rule;

abstract public class RuleFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, Rule, U> {
    private String id;

    /**
     * The ID of rule.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
