package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("launch-template")
public class LaunchTemplateResourceFinder extends AwsFinder<Ec2Client, LaunchTemplate, LaunchTemplateResource> {
    private String createTime;
    private String launchTemplateName;
    private String tagKey;
    private Map<String, String> tag;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLaunchTemplateName() {
        return launchTemplateName;
    }

    public void setLaunchTemplateName(String launchTemplateName) {
        this.launchTemplateName = launchTemplateName;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    @Override
    protected List<LaunchTemplate> findAllAws(Ec2Client client) {
        return client.describeLaunchTemplates().launchTemplates();
    }

    @Override
    protected List<LaunchTemplate> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeLaunchTemplates(r -> r.filters(createFilters(filters))).launchTemplates();
    }
}
