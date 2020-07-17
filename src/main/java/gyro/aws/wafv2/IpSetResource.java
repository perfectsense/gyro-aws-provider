/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.IPSet;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

@Type("wafv2-ip-set")
public class IpSetResource extends WafTaggableResource implements Copyable<IPSet> {

    private String name;
    private String ipAddressVersion;
    private String description;
    private Set<String> addresses;
    private String id;
    private String arn;
    private Integer hashCode;

    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    @ValidStrings({"IPV4", "IPV6"})
    public String getIpAddressVersion() {
        return ipAddressVersion;
    }

    public void setIpAddressVersion(String ipAddressVersion) {
        this.ipAddressVersion = ipAddressVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Required
    @Updatable
    public Set<String> getAddresses() {
        if (addresses == null) {
            addresses = new HashSet<>();
        }

        return addresses;
    }

    public void setAddresses(Set<String> addresses) {
        this.addresses = addresses;
    }

    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Output
    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    protected String getResourceArn() {
        return getArn();
    }

    @Override
    public void copyFrom(IPSet ipSet) {
        setArn(ipSet.arn());
        setAddresses(new HashSet<>(ipSet.addresses()));
        setDescription(ipSet.description());
        setHashCode(ipSet.hashCode());
        setId(ipSet.id());
        setIpAddressVersion(ipSet.ipAddressVersionAsString());
        setName(ipSet.name());
    }

    @Override
    public boolean doRefresh() {
        Wafv2Client client = createClient(Wafv2Client.class);

        GetIpSetResponse response = getIpSet(client);

        if (response != null) {
            copyFrom(response.ipSet());

            return true;
        }

        return false;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Wafv2Client client = createClient(Wafv2Client.class);

        CreateIpSetResponse ipSet = client.createIPSet(r -> r.name(getName())
            .addresses(getAddresses())
            .description(getDescription())
            .ipAddressVersion(getIpAddressVersion())
            .scope(getScope()));

        setArn(ipSet.summary().arn());
        setId(ipSet.summary().id());
        setHashCode(ipSet.summary().hashCode());
    }

    @Override
    public void doUpdate(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.updateIPSet(
            r -> r.id(getId())
                .name(getName())
                .scope(getScope())
                .lockToken(lockToken(client))
                .addresses(getAddresses())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Wafv2Client client = createClient(Wafv2Client.class);

        client.deleteIPSet(r -> r.id(getId()).name(getName()).scope(getScope()).lockToken(lockToken(client)));
    }

    private GetIpSetResponse getIpSet(Wafv2Client client) {
        try {
            return client.getIPSet(r -> r.id(getId()).name(getName()).scope(getScope()));
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }

    private String lockToken(Wafv2Client client) {
        String token = null;
        GetIpSetResponse response = getIpSet(client);

        if (response != null) {
            token = response.lockToken();
        }

        return token;
    }
}
