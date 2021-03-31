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
import software.amazon.awssdk.services.backup.model.Condition;
import software.amazon.awssdk.services.backup.model.ConditionType;

public class BackupCondition extends Diffable implements Copyable<Condition> {

    private String conditionKey;
    private ConditionType conditionType;
    private String conditionValue;

    /**
     * The key in a key-value pair.
     */
    @Required
    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    /**
     * The operation that is applied to a key-value pair used to filter resources in a selection.
     */
    @Required
    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    /**
     * The value in a key-value pair.
     */
    @Required
    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    @Override
    public void copyFrom(Condition model) {
        setConditionKey(model.conditionKey());
        setConditionType(model.conditionType());
        setConditionValue(model.conditionValue());
    }

    @Override
    public String primaryKey() {
        return String.format(
            "Type: %s, Key: %s, Value: %s",
            getConditionType(),
            getConditionKey(),
            getConditionValue());
    }

    Condition toCondition() {
        return Condition.builder().conditionKey(getConditionKey())
            .conditionType(getConditionType()).conditionValue(getConditionValue()).build();
    }
}
