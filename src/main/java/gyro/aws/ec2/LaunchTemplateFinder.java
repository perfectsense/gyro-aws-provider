/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.LaunchTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query launch template.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    launch-template: $(external-query aws::launch-template { launch-template-name: ''})
 */
@Type("launch-template")
public class LaunchTemplateFinder extends Ec2TaggableAwsFinder<Ec2Client, LaunchTemplate, LaunchTemplateResource> {
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
        return client.describeLaunchTemplatesPaginator().launchTemplates().stream().collect(Collectors.toList());
    }

    @Override
    protected List<LaunchTemplate> findAws(Ec2Client client, Map<String, String> filters) {
        if (filters.containsKey("launch-template-id")) {
            String id = filters.get("launch-template-id");
            filters.remove("launch-template-id");
            return client.describeLaunchTemplatesPaginator(r -> r.launchTemplateIds(id).filters(createFilters(filters))).launchTemplates().stream().collect(Collectors.toList());
        } else {
            return client.describeLaunchTemplatesPaginator(r -> r.filters(createFilters(filters))).launchTemplates().stream().collect(Collectors.toList());
        }
    }
}
