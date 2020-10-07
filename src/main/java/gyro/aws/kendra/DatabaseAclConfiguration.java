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

package gyro.aws.kendra;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.AclConfiguration;

public class DatabaseAclConfiguration extends Diffable implements Copyable<AclConfiguration> {

    private String allowedGroupsColumnName;

    /**
     * A list of groups, separated by semi-colons, that filters a query response based on user context.
     */
    @Required
    @Updatable
    public String getAllowedGroupsColumnName() {
        return allowedGroupsColumnName;
    }

    public void setAllowedGroupsColumnName(String allowedGroupsColumnName) {
        this.allowedGroupsColumnName = allowedGroupsColumnName;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AclConfiguration model) {
        setAllowedGroupsColumnName(model.allowedGroupsColumnName());
    }

    public AclConfiguration toAclConfiguration() {
        return AclConfiguration.builder().allowedGroupsColumnName(getAllowedGroupsColumnName()).build();
    }
}
