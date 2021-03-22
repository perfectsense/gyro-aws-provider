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
