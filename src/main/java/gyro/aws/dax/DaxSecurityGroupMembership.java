/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.SecurityGroupMembership;

public class DaxSecurityGroupMembership extends Diffable implements Copyable<SecurityGroupMembership> {

    private String securityGroupIdentifier;
    private String status;

    /**
     * The ID of the security group.
     */
    public String getSecurityGroupIdentifier() {
        return securityGroupIdentifier;
    }

    public void setSecurityGroupIdentifier(String securityGroupIdentifier) {
        this.securityGroupIdentifier = securityGroupIdentifier;
    }

    /**
     * The status of the security group.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void copyFrom(SecurityGroupMembership model) {
        setSecurityGroupIdentifier(model.securityGroupIdentifier());
        setStatus(model.status());
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getSecurityGroupIdentifier());
    }
}
