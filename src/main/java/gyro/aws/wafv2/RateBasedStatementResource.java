package gyro.aws.wafv2;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatement;

public class RateBasedStatementResource extends WafDiffable implements Copyable<RateBasedStatement> {

    private String aggregateKeyType;
    private Long limit;
    private StatementResource scopeDownStatement;

    public String getAggregateKeyType() {
        return aggregateKeyType;
    }

    public void setAggregateKeyType(String aggregateKeyType) {
        this.aggregateKeyType = aggregateKeyType;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public StatementResource getScopeDownStatement() {
        return scopeDownStatement;
    }

    public void setScopeDownStatement(StatementResource scopeDownStatement) {
        this.scopeDownStatement = scopeDownStatement;
    }

    @Override
    public void copyFrom(RateBasedStatement rateBasedStatement) {
        setAggregateKeyType(rateBasedStatement.aggregateKeyTypeAsString());
        setLimit(rateBasedStatement.limit());
        setHashCode(rateBasedStatement.hashCode());

        setScopeDownStatement(null);
        if (rateBasedStatement.scopeDownStatement() != null) {
            StatementResource statement = newSubresource(StatementResource.class);
            statement.copyFrom(rateBasedStatement.scopeDownStatement());
            setScopeDownStatement(statement);
        }
    }

    RateBasedStatement toRateBasedStatement() {
        return RateBasedStatement.builder()
            .aggregateKeyType(getAggregateKeyType())
            .limit(getLimit())
            .scopeDownStatement(getScopeDownStatement().toStatement())
            .build();
    }
}
