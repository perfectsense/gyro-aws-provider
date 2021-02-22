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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.route53.HostedZoneResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class DnsEntry extends Diffable implements Copyable<software.amazon.awssdk.services.ec2.model.DnsEntry> {
    private String name;
    private HostedZoneResource hostedZone;

    /**
     * The name of the dns.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The associated hosted zone.
     */
    @Output
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.DnsEntry dnsEntry) {
        setName(dnsEntry.dnsName());
        setHostedZone(!ObjectUtils.isBlank(dnsEntry.hostedZoneId())
            ? findById(HostedZoneResource.class, dnsEntry.hostedZoneId()) : null);
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s",getName(), getHostedZone() != null ? getHostedZone().getId() : "");
    }
}
