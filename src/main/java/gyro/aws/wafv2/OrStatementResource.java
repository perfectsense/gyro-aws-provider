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

package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMin;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.OrStatement;

public class OrStatementResource extends Diffable implements Copyable<OrStatement> {

    private Set<StatementResource> statement;

    /**
     * The set of statement resource associated with the OR statement. (Required)
     *
     * @subresource gyro.aws.wafv2.StatementResource
     */
    @Required
    @Updatable
    @CollectionMin(2)
    public Set<StatementResource> getStatement() {
        if (statement == null) {
            statement = new HashSet<>();
        }

        return statement;
    }

    public void setStatement(Set<StatementResource> statement) {
        this.statement = statement;
    }

    @Override
    public String primaryKey() {
        return getStatement().stream().map(StatementResource::primaryKey)
            .sorted()
            .collect(Collectors.joining(" or "));
    }

    @Override
    public void copyFrom(OrStatement orStatement) {
        getStatement().clear();
        orStatement.statements().forEach(o -> {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(o);
            getStatement().add(statement);
        });
    }

    OrStatement toOrStatement() {
        return OrStatement.builder()
            .statements(getStatement().stream().map(StatementResource::toStatement).collect(Collectors.toList()))
            .build();
    }
}
