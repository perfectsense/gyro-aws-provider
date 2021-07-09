/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import org.apache.commons.lang3.StringUtils;

public class AcceleratorIpSet extends Diffable implements Copyable<software.amazon.awssdk.services.globalaccelerator.model.IpSet> {

    private List<String> ipAddresses;
    private String ipFamily;

    /**
     * Static IP addresses associated with the accelerator.
     */
    public List<String> getIpAddresses() {
        if (ipAddresses == null) {
            ipAddresses = new ArrayList<>();
        }

        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    /**
     * The family of the IP address. Currently only IPv4.
     */
    public String getIpFamily() {
        return ipFamily;
    }

    public void setIpFamily(String ipFamily) {
        this.ipFamily = ipFamily;
    }

    @Override
    public String primaryKey() {
        return StringUtils.join(ipAddresses, ", ");
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.globalaccelerator.model.IpSet ipSet) {
        setIpAddresses(ipSet.ipAddresses());
        setIpFamily(ipSet.ipFamily());
    }
}
