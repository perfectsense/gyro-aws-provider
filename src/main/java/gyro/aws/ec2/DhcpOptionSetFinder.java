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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DhcpOptions;

/**
 * Query dhcp options.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dhcp-options: $(external-query aws::dhcp-options { dhcp-options-id: ''})
 */
@Type("dhcp-options")
public class DhcpOptionSetFinder extends Ec2TaggableAwsFinder<Ec2Client, DhcpOptions, DhcpOptionSetResource> {

    private String dhcpOptionsId;
    private String key;
    private String value;
    private String ownerId;
    private Map<String, String> tag;
    private String tagKey;

    /**
     * The ID of a DHCP options set.
     */
    public String getDhcpOptionsId() {
        return dhcpOptionsId;
    }

    public void setDhcpOptionsId(String dhcpOptionsId) {
        this.dhcpOptionsId = dhcpOptionsId;
    }

    /**
     * The key for one of the options (for example, domain-name).
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value for one of the options.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The ID of the AWS account that owns the DHCP options set.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
    protected List<DhcpOptions> findAllAws(Ec2Client client) {
        return client.describeDhcpOptionsPaginator().dhcpOptions().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DhcpOptions> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeDhcpOptionsPaginator(r -> r.filters(createFilters(filters)))
            .dhcpOptions().stream().collect(Collectors.toList());
    }
}
