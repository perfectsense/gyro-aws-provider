/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class DeadLetterConfig extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.DeadLetterConfig> {

    private String arn;

    @Required
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.DeadLetterConfig model) {
        setArn(model.arn());
    }

    @Override
    public String primaryKey() {
        return getArn();
    }

    protected software.amazon.awssdk.services.eventbridge.model.DeadLetterConfig toDeadLetterConfig() {
        return software.amazon.awssdk.services.eventbridge.model.DeadLetterConfig
            .builder().arn(getArn()).build();
    }
}
