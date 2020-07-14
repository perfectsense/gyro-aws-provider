package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.OrStatement;

public class OrStatementResource extends WafDiffable implements Copyable<OrStatement> {

    private Set<StatementResource> statement;

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
    public void copyFrom(OrStatement orStatement) {
        getStatement().clear();
        orStatement.statements().forEach(o -> {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(o);
            getStatement().add(statement);
        });

        setHashCode(orStatement.hashCode());
    }

    OrStatement toOrStatement() {
        return OrStatement.builder()
            .statements(getStatement().stream().map(StatementResource::toStatement).collect(Collectors.toList()))
            .build();
    }
}
