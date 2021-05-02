/*
 * Copyright 2021, Perfect Sense.
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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptionsRequest;

public class LaunchTemplateHibernationOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptions> {

    private Boolean configured;

    /**
     * When set to ``true``, the instance in enabled for hibernation.
     */
    @Required
    @Updatable
    public Boolean getConfigured() {
        return configured;
    }

    public void setConfigured(Boolean configured) {
        this.configured = configured;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptions model) {
        setConfigured(model.configured());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateHibernationOptionsRequest toLaunchTemplateHibernationOptionsRequest() {
        return LaunchTemplateHibernationOptionsRequest.builder().configured(getConfigured()).build();
    }
}
