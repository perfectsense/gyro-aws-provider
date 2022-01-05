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
import software.amazon.awssdk.services.eventbridge.model.SqsParameters;

public class SqsParameter extends Diffable implements Copyable<SqsParameters> {

    private String messageGroupId;

    @Required
    public String getMessageGroupId() {
        return messageGroupId;
    }

    public void setMessageGroupId(String messageGroupId) {
        this.messageGroupId = messageGroupId;
    }

    @Override
    public void copyFrom(SqsParameters model) {
        setMessageGroupId(model.messageGroupId());
    }

    @Override
    public String primaryKey() {
        return getMessageGroupId();
    }

    protected SqsParameters toSqsParameters() {
        return SqsParameters.builder().messageGroupId(getMessageGroupId()).build();
    }
}
