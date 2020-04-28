/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import gyro.core.GyroException;
import gyro.core.LockBackend;
import gyro.core.Type;
import gyro.core.auth.CredentialsSettings;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Type("dynamo-db")
public class DynamoDbLockBackend extends LockBackend {

    private String tableName;
    private String lockKey;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getLockKey() {
        return lockKey != null ? lockKey : "default";
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    @Override
    public void lock(String lockId) throws Exception {
        DynamoDbClient client = client();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("LockKey", AttributeValue.builder().s(getLockKey()).build());
        item.put("GyroId", AttributeValue.builder().s(lockId).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(getTableName())
            .item(item)
            .conditionExpression("attribute_not_exists(LockKey)")
            .build();

        try {
            client.putItem(putItemRequest);
        } catch (ConditionalCheckFailedException ex) {
            throw new GyroException(String.format("State is currently locked!%s", getCurrentLockIdString()));
        }
    }

    @Override
    public void unlock(String lockId) throws Exception {
        DynamoDbClient client = client();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("LockKey", AttributeValue.builder().s(getLockKey()).build());

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", AttributeValue.builder().s(lockId).build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
            .tableName(getTableName())
            .key(item)
            .conditionExpression("GyroId = :id")
            .expressionAttributeValues(expressionAttributeValues)
            .build();

        try {
            client.deleteItem(deleteItemRequest);

        } catch (ConditionalCheckFailedException ex) {
            throw new GyroException(String.format(
                "Cannot unlock '%s' as it is no longer the active lock!%s", lockId, getCurrentLockIdString()));
        }
    }

    @Override
    public void updateLockInfo(String lockId, String info) throws Exception {
        DynamoDbClient client = client();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("LockKey", AttributeValue.builder().s(getLockKey()).build());

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":id", AttributeValue.builder().s(lockId).build());
        expressionAttributeValues.put(":info", AttributeValue.builder().s(info).build());

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
            .tableName(getTableName())
            .key(item)
            .conditionExpression("GyroId = :id")
            .updateExpression("SET GyroLockInfo = :info")
            .expressionAttributeValues(expressionAttributeValues)
            .build();

        try {
            client.updateItem(updateItemRequest);

        } catch (ConditionalCheckFailedException ex) {
            throw new GyroException(String.format(
                "Cannot update info for '%s' as it is no longer the active lock!%s",
                lockId,
                getCurrentLockIdString()));
        }
    }

    private String getCurrentLockIdString() {
        Optional<Map<String, AttributeValue>> currentLock = getCurrentLock();
        return String.join(
            "\n",
            currentLock.map(this::getLockId).map(id -> String.format("\nCurrent lock ID: '%s'.", id)).orElse(""),
            currentLock.map(this::getLockInfo).orElse(""));
    }

    private String getLockInfo(Map<String, AttributeValue> currentLock) {
        return Optional.ofNullable(currentLock.get("GyroLockInfo")).map(AttributeValue::s).orElse(null);
    }

    private String getLockId(Map<String, AttributeValue> currentLock) {
        return Optional.ofNullable(currentLock.get("GyroId")).map(AttributeValue::s).orElse(null);
    }

    private Optional<Map<String, AttributeValue>> getCurrentLock() {
        DynamoDbClient client = client();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("LockKey", AttributeValue.builder().s(getLockKey()).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
            .tableName(getTableName())
            .key(item)
            .build();

        return Optional.ofNullable(client.getItem(getItemRequest).item());
    }

    private DynamoDbClient client() {
        return AwsResource.createClient(DynamoDbClient.class, credentials());
    }

    private AwsCredentials credentials() {
        return (AwsCredentials) getRootScope().getSettings(CredentialsSettings.class)
            .getCredentialsByName()
            .get("aws::default");
    }
}
