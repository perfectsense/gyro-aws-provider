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
import software.amazon.awssdk.services.wafv2.model.NotStatement;

public class NotStatementResource extends WafDiffable implements Copyable<NotStatement> {

    private StatementResource statement;

    public StatementResource getStatement() {
        return statement;
    }

    public void setStatement(StatementResource statement) {
        this.statement = statement;
    }

    @Override
    public void copyFrom(NotStatement notStatement) {
        setStatement(null);
        if (notStatement.statement() != null) {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(notStatement.statement());
            setStatement(statement);
        }

        setHashCode(notStatement.hashCode());
    }

    NotStatement toNotStatement() {
        return NotStatement.builder()
            .statement(getStatement().toStatement())
            .build();
    }
}
