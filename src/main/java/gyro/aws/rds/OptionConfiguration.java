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

package gyro.aws.rds;

import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import java.util.HashSet;
import java.util.Set;

public class OptionConfiguration extends Diffable {

    private String optionName;
    private Set<OptionSettings> optionSettings;
    private Integer port;
    private String version;
    private Set<SecurityGroupResource> vpcSecurityGroups;

    /**
     * The name of the option.
     */
    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    /**
     * The List of option settings to include in the option configuration.
     *
     * @subresource gyro.aws.rds.OptionSettings
     */
    @Updatable
    public Set<OptionSettings> getOptionSettings() {
        if (optionSettings == null) {
            optionSettings = new HashSet<>();
        }

        return optionSettings;
    }

    public void setOptionSettings(Set<OptionSettings> optionSettings) {
        this.optionSettings = optionSettings;
    }

    /**
     * The port of the option.
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The version of the option.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * A list of VPC security groups used for this option.
     */
    public Set<SecurityGroupResource> getVpcSecurityGroups() {
        if (vpcSecurityGroups == null) {
            vpcSecurityGroups = new HashSet<>();
        }

        return vpcSecurityGroups;
    }

    public void setVpcSecurityGroups(Set<SecurityGroupResource> vpcSecurityGroups) {
        this.vpcSecurityGroups = vpcSecurityGroups;
    }

    @Override
    public String primaryKey() {
        return getOptionName();
    }

}
