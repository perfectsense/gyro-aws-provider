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

package gyro.aws.dynamodb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.docdb.DocDbFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/**
 * Query dynamodb-table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dynamodb-table: $(external-query aws::dynamodb-table { name: "dynamo-db-example"})
 */
@Type("dynamodb-table")
public class DynamoDbTableFinder extends DocDbFinder<DynamoDbClient, TableDescription, DynamoDbTableResource> {

    private String name;

    /**
     * The DynamoDb table name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<TableDescription> findAllAws(DynamoDbClient client) {
        return client.listTablesPaginator().tableNames().stream()
            .map(t -> client.describeTable(r -> r.tableName(t)).table())
            .collect(Collectors.toList());
    }

    @Override
    protected List<TableDescription> findAws(DynamoDbClient client, Map<String, String> filters) {
        try {
            return Collections.singletonList(client.describeTable(r -> r.tableName(filters.get("name"))).table());
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
