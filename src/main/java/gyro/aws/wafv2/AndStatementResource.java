/*
 * Copyright 2020, Brightspot.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.AndStatement;

public class AndStatementResource extends Diffable implements Copyable<AndStatement> {

    private Set<StatementResource> statement;

    /**
     * The set of statement resource associated with the AND statement.
     */
    @Required
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
            .collect(Collectors.joining(" and "));
    }

    @Override
    public void copyFrom(AndStatement andStatement) {
        getStatement().clear();

        andStatement.statements().forEach(o -> {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(o);
            getStatement().add(statement);
        });
    }

    AndStatement toAndStatement() {
        return AndStatement.builder()
            .statements(getStatement().stream().map(StatementResource::toStatement).collect(Collectors.toList()))
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getStatement().stream().anyMatch(StatementResource::isRuleRateBased)) {
            errors.add(new ValidationError(
                this,
                "statement",
                "Rate based rule cannot be set as part of a 'or-statement'"));
        }

        return errors;
    }
}
