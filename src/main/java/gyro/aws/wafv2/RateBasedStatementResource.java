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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatement;

public class RateBasedStatementResource extends Diffable implements Copyable<RateBasedStatement> {

    private String aggregateKeyType;
    private Long limit;
    private StatementResource scopeDownStatement;

    /**
     * The aggregate key type for the rate based statement. Currently only supported value is ``IP``. Defaults to ``IP``.
     */
    @ValidStrings("IP")
    public String getAggregateKeyType() {
        if (aggregateKeyType == null) {
            aggregateKeyType = "IP";
        }

        return aggregateKeyType;
    }

    public void setAggregateKeyType(String aggregateKeyType) {
        this.aggregateKeyType = aggregateKeyType;
    }

    /**
     * The rate limit for the rate based statement. Minimum value is ``100``. (Required)
     */
    @Required
    @Min(100)
    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    /**
     * The statement resource associated with the rate based statement.
     *
     * @subresource gyro.aws.wafv2.StatementResource
     */
    public StatementResource getScopeDownStatement() {
        return scopeDownStatement;
    }

    public void setScopeDownStatement(StatementResource scopeDownStatement) {
        this.scopeDownStatement = scopeDownStatement;
    }

    @Override
    public String primaryKey() {
        return String.format(
            " with limit - %s%s",
            getLimit(),
            (getScopeDownStatement() != null ? String.format(
                " and statement - [%s]",
                getScopeDownStatement().primaryKey()) : ""));
    }

    @Override
    public void copyFrom(RateBasedStatement rateBasedStatement) {
        setAggregateKeyType(rateBasedStatement.aggregateKeyTypeAsString());
        setLimit(rateBasedStatement.limit());

        setScopeDownStatement(null);
        if (rateBasedStatement.scopeDownStatement() != null) {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(rateBasedStatement.scopeDownStatement());
            setScopeDownStatement(statement);
        }
    }

    RateBasedStatement toRateBasedStatement() {
        RateBasedStatement.Builder builder = RateBasedStatement.builder()
            .aggregateKeyType(getAggregateKeyType())
            .limit(getLimit());

        if (getScopeDownStatement() != null) {
            builder = builder.scopeDownStatement(getScopeDownStatement().toStatement());

        }

        return builder.build();
    }
}
