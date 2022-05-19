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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class RunCommandTarget extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.RunCommandTarget> {

    private String key;
    private List<String> values;

    /**
     * Tag-key or InstanceIds.
     */
    @Required
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * If key is InstanceIds, then this is a list of Amazon EC2 instance IDs.
     */
    @Required
    @Updatable
    public List<String> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }

        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.RunCommandTarget model) {
        setKey(model.key());
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return getKey();
    }

    protected software.amazon.awssdk.services.eventbridge.model.RunCommandTarget toRunCommandTarget() {
        return software.amazon.awssdk.services.eventbridge.model.RunCommandTarget.builder()
            .key(getKey())
            .values(getValues())
            .build();
    }
}
