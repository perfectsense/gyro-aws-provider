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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.ElasticGpuSpecification;

public class LaunchTemplateElasticGpuSpecification extends Diffable implements Copyable<ElasticGpuSpecification> {

    private String type;

    /**
     * The type of Elastic Graphics accelerator. See `Elastic Graphics Basics <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/elastic-graphics.html#elastic-graphics-basics/>`_.
     */
    @Required
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ElasticGpuSpecification model) {
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    ElasticGpuSpecification toElasticGpuSpecification() {
        return ElasticGpuSpecification.builder().type(getType()).build();
    }
}
