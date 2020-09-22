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

package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.model.IPSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateIpSetRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class IpSetDescriptorResource extends AbstractWafResource implements Copyable<IPSetDescriptor> {
    private String value;
    private String type;

    /**
     * The ip to be filtered on. (Required)
     */
    @Required
    public String getValue() {
        return getCidrValue();
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of ip provided. Valid values are ``IPV4`` or ``IPV6``. (Required)
     */
    @Required
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(IPSetDescriptor ipSetDescriptor) {
        setType(ipSetDescriptor.typeAsString());
        setValue(ipSetDescriptor.value());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        saveIpSetDescriptor(toIpSetDescriptor(), false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        saveIpSetDescriptor(toIpSetDescriptor(), true);

    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getType(), getValue());
    }

    protected abstract void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete);

    private IPSetDescriptor toIpSetDescriptor() {
        return IPSetDescriptor.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    protected UpdateIpSetRequest.Builder toUpdateIpSetRequest(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        IpSetResource parent = (IpSetResource) parent();

        IPSetUpdate ipSetUpdate = IPSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .ipSetDescriptor(ipSetDescriptor)
            .build();

        return UpdateIpSetRequest.builder()
            .ipSetId(parent.getId())
            .updates(ipSetUpdate);
    }

    private String getCidrValue() {
        String cidr = value;

        boolean isValid = false;

        if (!ObjectUtils.isBlank(cidr) && cidr.split("/").length == 2) {
            isValid = true;
        }

        if (isValid) {
            isValid = false;
            String ip = cidr.split("/")[0];
            String range = cidr.split("/")[1];
            int rangeInt = 0;
            try {
                rangeInt = Integer.parseInt(range);
            } catch (NumberFormatException e) {
                // fails anyway
            }

            if (getType().equals("IPV4")) {
                isValid = (range.equals("8") || (16 <= rangeInt && rangeInt <= 32));
            } else if (getType().equals("IPV6")) {
                isValid = (range.equals("24") || range.equals("32")
                    || range.equals("48") || range.equals("56") || range.equals("64") || range.equals("128"));
            }

            if (isValid) {
                try {
                    InetAddress byName = InetAddress.getByName(ip);
                    String hostAddress = byName.getHostAddress();
                    if (getType().equals("IPV6")) {
                        isValid = !ip.contains(".") && ip.contains(":");
                        ip = Arrays.stream(hostAddress.split(":")).map(o -> StringUtils.leftPad(o, 4, "0")).collect(Collectors.joining(":"));
                    } else if (getType().equals("IPV4")) {
                        isValid = ip.contains(".") && !ip.contains(":");
                    }

                    cidr = ip + "/" + range;
                } catch (UnknownHostException ex) {
                    isValid = false;
                }
            }

            if (!isValid) {
                throw new GyroException(String.format("Invalid cidr - %s of type %s.", value, getType()));
            }
        }

        return cidr;
    }
}
