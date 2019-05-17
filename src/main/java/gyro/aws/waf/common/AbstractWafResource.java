package gyro.aws.waf.common;

import gyro.aws.AwsResource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public abstract class AbstractWafResource extends AwsResource {
    protected WafRegionalClient getRegionalClient() {
        return createClient(WafRegionalClient.class);
    }

    protected WafClient getGlobalClient() {
        return createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);
    }
}
