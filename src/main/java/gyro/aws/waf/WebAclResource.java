package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.lang.Resource;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.WafAction;
import software.amazon.awssdk.services.waf.model.WebACL;

import java.util.Set;
import java.util.UUID;

public class WebAclResource extends AwsResource {
    private String name;
    private String metricName;
    private String defaultAction;
    private String webAclId;
    private String arn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public String getWebAclId() {
        return webAclId;
    }

    public void setWebAclId(String webAclId) {
        this.webAclId = webAclId;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public boolean refresh() {
        WafClient client = createClient(WafClient.class);

        GetWebAclResponse response = client.getWebACL(
            r -> r.webACLId(getWebAclId())
        );

        WebACL webACL = response.webACL();
        webACL.webACLId();
        webACL.webACLArn();
        webACL.defaultAction();
        webACL.metricName();
        webACL.name();
        webACL.rules();

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class);

        CreateWebAclResponse response = client.createWebACL(
            r -> r.changeToken(UUID.randomUUID().toString())
                .name(getName())
                .metricName(getMetricName())
                .defaultAction(WafAction.builder().type(getDefaultAction()).build())
        );

        WebACL webACL = response.webACL();
        setArn(webACL.webACLArn());
        setWebAclId(webACL.webACLId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class);

        client.deleteWebACL(
            r -> r.changeToken(UUID.randomUUID().toString())
                .webACLId(getWebAclId())
        );
    }

    @Override
    public String toDisplayString() {
        return null;
    }
}
