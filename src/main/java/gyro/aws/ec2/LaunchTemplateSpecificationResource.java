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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateSpecification;

public class LaunchTemplateSpecificationResource extends Diffable implements Copyable<LaunchTemplateSpecification> {

    private LaunchTemplateResource launchTemplate;
    private String version;

    /**
     * The launch template to use for creating the instance.
     */
    @Required
    public LaunchTemplateResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    /**
     * The version of the launch template to use.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void copyFrom(LaunchTemplateSpecification launchTemplateSpecification) {
        setLaunchTemplate(findById(LaunchTemplateResource.class,launchTemplateSpecification.launchTemplateId()));
        setVersion(launchTemplateSpecification.version());
    }

    @Override
    public String primaryKey() {
        return "launch template specification";
    }

    LaunchTemplateSpecification toLaunchTemplateSpecification() {
        return LaunchTemplateSpecification.builder()
            .version(getVersion())
            .launchTemplateId(getLaunchTemplate().getId())
            .build();
    }
}
