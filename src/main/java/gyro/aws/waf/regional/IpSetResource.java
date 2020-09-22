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

package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a regional IP match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-ip-set-regional ip-set-example
 *         name: "ip-set-example"
 *
 *         ip-set-descriptor
 *             type: "IPV4"
 *             value: "190.0.0.26/32"
 *         end
 *     end
 */
@Type("waf-ip-set-regional")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {
    private Set<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.IpSetDescriptorResource
     */
    @Required
    @Updatable
    public Set<IpSetDescriptorResource> getIpSetDescriptor() {
        if (ipSetDescriptor == null) {
            ipSetDescriptor = new HashSet<>();
        }

        return ipSetDescriptor;
    }

    public void setIpSetDescriptor(Set<IpSetDescriptorResource> ipSetDescriptor) {
        this.ipSetDescriptor = ipSetDescriptor;
    }

    @Override
    public void copyFrom(IPSet ipSet) {
        setId(ipSet.ipSetId());
        setName(ipSet.name());

        getIpSetDescriptor().clear();
        for (IPSetDescriptor ipSetDescriptor : ipSet.ipSetDescriptors()) {
            IpSetDescriptorResource ipSetDescriptorResource = newSubresource(IpSetDescriptorResource.class);
            ipSetDescriptorResource.copyFrom(ipSetDescriptor);
            getIpSetDescriptor().add(ipSetDescriptorResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetIpSetResponse response = getRegionalClient().getIPSet(
            r -> r.ipSetId(getId())
        );

        this.copyFrom(response.ipSet());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        CreateIpSetResponse response = client.createIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.ipSet().ipSetId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        client.deleteIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(getId())
        );
    }
}
