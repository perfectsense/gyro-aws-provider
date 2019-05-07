package gyro.aws.waf;

import gyro.aws.AwsResource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public abstract class AbstractWafResource extends AwsResource {
    private Boolean regionalWaf;

    public Boolean getRegionalWaf() {
        if (regionalWaf == null) {
            regionalWaf = false;
        }

        return regionalWaf;
    }

    public void setRegionalWaf(Boolean regionalWaf) {
        this.regionalWaf = regionalWaf;
    }

    WafRegionalClient getRegionalClient() {
        return createClient(WafRegionalClient.class);
    }

    WafClient getGlobalClient() {
        return createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);
    }
}
