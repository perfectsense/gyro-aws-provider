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

package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.aws.ec2.LaunchTemplateResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.eks.model.LaunchTemplateSpecification;

public class EksLaunchTemplateSpecification extends Diffable implements Copyable<LaunchTemplateSpecification> {

    private LaunchTemplateResource launchTemplate;
    private String version;

    /**
     * The ID of the launch template.
     */
    public LaunchTemplateResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    /**
     * The version of the launch template to use.
     */
    @Updatable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void copyFrom(LaunchTemplateSpecification model) {
        setLaunchTemplate(findById(LaunchTemplateResource.class, model.id()));
        setVersion(model.version());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateSpecification toLaunchTemplateSpecification() {
        return LaunchTemplateSpecification.builder().id(getLaunchTemplate().getId()).version(getVersion()).build();
    }
}
