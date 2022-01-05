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

public class InputTransformer extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.InputTransformer> {

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.InputTransformer model) {
        model.inputPathsMap();
        model.inputTemplate();
    }

    @Override
    public String primaryKey() {
        return null;
    }
}
