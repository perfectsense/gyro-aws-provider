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
