package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeLaunchTemplatesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeLaunchTemplatesResponse;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query launch template.
 *
 * .. code-block:: gyro
 *
 *    launch-template: $(aws::launch-template EXTERNAL/* | launch-template-name = '')
 */
@Type("launch-template")
public class LaunchTemplateFinder extends AwsFinder<Ec2Client, LaunchTemplate, LaunchTemplateResource> {
    private String createTime;
    private String launchTemplateName;
    private String launchTemplateId;
    private String tagKey;
    private Map<String, String> tag;

    /**
     * The time the launch template was created.
     */
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * The ID of the launch template.
     */
    public String getLaunchTemplateId() {
        return launchTemplateId;
    }

    public void setLaunchTemplateId(String launchTemplateId) {
        this.launchTemplateId = launchTemplateId;
    }

    /**
     * The name of the launch template.
     */
    public String getLaunchTemplateName() {
        return launchTemplateName;
    }

    public void setLaunchTemplateName(String launchTemplateName) {
        this.launchTemplateName = launchTemplateName;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    @Override
    protected List<LaunchTemplate> findAllAws(Ec2Client client) {
        return getLaunchTemplates(client, null);
    }

    @Override
    protected List<LaunchTemplate> findAws(Ec2Client client, Map<String, String> filters) {
        return getLaunchTemplates(client, filters);
    }

    private List<LaunchTemplate> getLaunchTemplates(Ec2Client client, Map<String, String> filters) {
        List<LaunchTemplate> launchTemplates = new ArrayList<>();

        DescribeLaunchTemplatesRequest.Builder builder = DescribeLaunchTemplatesRequest.builder();

        if (filters != null) {
            if (filters.containsKey("launch-template-id")) {
                builder = builder.launchTemplateIds(filters.get("launch-template-id"));
                filters.remove("launch-template-id");
            }

            builder = builder.filters(createFilters(filters));
        }

        String marker = null;
        DescribeLaunchTemplatesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeLaunchTemplates(builder.build());
            } else {
                response = client.describeLaunchTemplates(builder.nextToken(marker).build());
            }

            marker = response.nextToken();
            launchTemplates.addAll(response.launchTemplates());
        } while (!ObjectUtils.isBlank(marker));

        return launchTemplates;
    }
}
