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

package gyro.aws.backup;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.backup.model.Condition;
import software.amazon.awssdk.services.backup.model.ConditionType;

public class BackupCondition extends Diffable implements Copyable<Condition> {

    private String key;
    private ConditionType type;
    private String value;

    /**
     * The key in a key-value pair.
     */
    @Required
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The operation that is applied to a key-value pair used to filter resources in a selection.
     */
    @Required
    @ValidStrings("STRINGEQUALS")
    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    /**
     * The value in a key-value pair.
     */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(Condition model) {
        setKey(model.conditionKey());
        setType(model.conditionType());
        setValue(model.conditionValue());
    }

    @Override
    public String primaryKey() {
        return String.format(
            "with type: %s, key: %s and value: %s",
            getType(),
            getKey(),
            getValue());
    }

    Condition toCondition() {
        return Condition.builder().conditionKey(getKey())
            .conditionType(getType()).conditionValue(getValue()).build();
    }
}
