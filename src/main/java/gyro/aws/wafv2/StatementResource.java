package gyro.aws.wafv2;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.Statement;

public class StatementResource extends WafDiffable implements Copyable<Statement> {

    private AndStatementResource andStatement;
    private NotStatementResource notStatement;
    private OrStatementResource orStatement;
    private ByteMatchStatementResource byteMatchStatement;
    private GeoMatchStatementResource geoMatchStatement;
    private IpSetReferenceStatementResource ipSetReferenceStatement;
    private RegexPatternSetReferenceStatementResource regexPatternSetReferenceStatement;
    private SizeConstraintStatementResource sizeConstraintStatement;
    private SqliMatchStatementResource sqliMatchStatement;
    private XssMatchStatementResource xssMatchStatement;
    private RateBasedStatementResource rateBasedStatement;
    private ManagedRuleGroupStatementResource managedRuleGroupStatement;
    private RuleGroupReferenceStatementResource ruleGroupReferenceStatement;

    public AndStatementResource getAndStatement() {
        return andStatement;
    }

    public void setAndStatement(AndStatementResource andStatement) {
        this.andStatement = andStatement;
    }

    public NotStatementResource getNotStatement() {
        return notStatement;
    }

    public void setNotStatement(NotStatementResource notStatement) {
        this.notStatement = notStatement;
    }

    public OrStatementResource getOrStatement() {
        return orStatement;
    }

    public void setOrStatement(OrStatementResource orStatement) {
        this.orStatement = orStatement;
    }

    public ByteMatchStatementResource getByteMatchStatement() {
        return byteMatchStatement;
    }

    public void setByteMatchStatement(ByteMatchStatementResource byteMatchStatement) {
        this.byteMatchStatement = byteMatchStatement;
    }

    public GeoMatchStatementResource getGeoMatchStatement() {
        return geoMatchStatement;
    }

    public void setGeoMatchStatement(GeoMatchStatementResource geoMatchStatement) {
        this.geoMatchStatement = geoMatchStatement;
    }

    public IpSetReferenceStatementResource getIpSetReferenceStatement() {
        return ipSetReferenceStatement;
    }

    public void setIpSetReferenceStatement(IpSetReferenceStatementResource ipSetReferenceStatement) {
        this.ipSetReferenceStatement = ipSetReferenceStatement;
    }

    public RegexPatternSetReferenceStatementResource getRegexPatternSetReferenceStatement() {
        return regexPatternSetReferenceStatement;
    }

    public void setRegexPatternSetReferenceStatement(RegexPatternSetReferenceStatementResource regexPatternSetReferenceStatement) {
        this.regexPatternSetReferenceStatement = regexPatternSetReferenceStatement;
    }

    public SizeConstraintStatementResource getSizeConstraintStatement() {
        return sizeConstraintStatement;
    }

    public void setSizeConstraintStatement(SizeConstraintStatementResource sizeConstraintStatement) {
        this.sizeConstraintStatement = sizeConstraintStatement;
    }

    public SqliMatchStatementResource getSqliMatchStatement() {
        return sqliMatchStatement;
    }

    public void setSqliMatchStatement(SqliMatchStatementResource sqliMatchStatement) {
        this.sqliMatchStatement = sqliMatchStatement;
    }

    public XssMatchStatementResource getXssMatchStatement() {
        return xssMatchStatement;
    }

    public void setXssMatchStatement(XssMatchStatementResource xssMatchStatement) {
        this.xssMatchStatement = xssMatchStatement;
    }

    public RateBasedStatementResource getRateBasedStatement() {
        return rateBasedStatement;
    }

    public void setRateBasedStatement(RateBasedStatementResource rateBasedStatement) {
        this.rateBasedStatement = rateBasedStatement;
    }

    public ManagedRuleGroupStatementResource getManagedRuleGroupStatement() {
        return managedRuleGroupStatement;
    }

    public void setManagedRuleGroupStatement(ManagedRuleGroupStatementResource managedRuleGroupStatement) {
        this.managedRuleGroupStatement = managedRuleGroupStatement;
    }

    public RuleGroupReferenceStatementResource getRuleGroupReferenceStatement() {
        return ruleGroupReferenceStatement;
    }

    public void setRuleGroupReferenceStatement(RuleGroupReferenceStatementResource ruleGroupReferenceStatement) {
        this.ruleGroupReferenceStatement = ruleGroupReferenceStatement;
    }

    @Override
    public void copyFrom(Statement statement) {
        setAndStatement(null);
        if (statement.andStatement() != null) {
            AndStatementResource andStatement = newSubresource(AndStatementResource.class);
            andStatement.copyFrom(statement.andStatement());
            setAndStatement(andStatement);
        }

        setNotStatement(null);
        if (statement.notStatement() != null) {
            NotStatementResource notStatement = newSubresource(NotStatementResource.class);
            notStatement.copyFrom(statement.notStatement());
            setNotStatement(notStatement);
        }

        setOrStatement(null);
        if (statement.orStatement() != null) {
            OrStatementResource orStatement = newSubresource(OrStatementResource.class);
            orStatement.copyFrom(statement.orStatement());
            setOrStatement(orStatement);
        }

        setByteMatchStatement(null);
        if (statement.byteMatchStatement() != null) {
            ByteMatchStatementResource byteMatchStatement = newSubresource(ByteMatchStatementResource.class);
            byteMatchStatement.copyFrom(statement.byteMatchStatement());
            setByteMatchStatement(byteMatchStatement);
        }

        setGeoMatchStatement(null);
        if (statement.geoMatchStatement() != null) {
            GeoMatchStatementResource geoMatchStatement = newSubresource(GeoMatchStatementResource.class);
            geoMatchStatement.copyFrom(statement.geoMatchStatement());
            setGeoMatchStatement(geoMatchStatement);
        }

        setIpSetReferenceStatement(null);
        if (statement.ipSetReferenceStatement() != null) {
            IpSetReferenceStatementResource ipSetReferenceStatement = newSubresource(IpSetReferenceStatementResource.class);
            ipSetReferenceStatement.copyFrom(statement.ipSetReferenceStatement());
            setIpSetReferenceStatement(ipSetReferenceStatement);
        }

        setRegexPatternSetReferenceStatement(null);
        if (statement.regexPatternSetReferenceStatement() != null) {
            RegexPatternSetReferenceStatementResource regexPatternSetReferenceStatement = newSubresource(
                RegexPatternSetReferenceStatementResource.class);
            regexPatternSetReferenceStatement.copyFrom(statement.regexPatternSetReferenceStatement());
            setRegexPatternSetReferenceStatement(regexPatternSetReferenceStatement);
        }


        setSizeConstraintStatement(null);
        if (statement.sizeConstraintStatement() != null) {
            SizeConstraintStatementResource sizeConstraintStatement = newSubresource(SizeConstraintStatementResource.class);
            sizeConstraintStatement.copyFrom(statement.sizeConstraintStatement());
            setSizeConstraintStatement(sizeConstraintStatement);
        }

        setSqliMatchStatement(null);
        if (statement.sqliMatchStatement() != null) {
            SqliMatchStatementResource sqliMatchStatement = newSubresource(SqliMatchStatementResource.class);
            sqliMatchStatement.copyFrom(statement.sqliMatchStatement());
            setSqliMatchStatement(sqliMatchStatement);
        }

        setXssMatchStatement(null);
        if (statement.xssMatchStatement() != null) {
            XssMatchStatementResource xssMatchStatement = newSubresource(XssMatchStatementResource.class);
            xssMatchStatement.copyFrom(statement.xssMatchStatement());
            setXssMatchStatement(xssMatchStatement);
        }

        setRateBasedStatement(null);
        if (statement.rateBasedStatement() != null) {
            RateBasedStatementResource rateBasedStatement = newSubresource(RateBasedStatementResource.class);
            rateBasedStatement.copyFrom(statement.rateBasedStatement());
            setRateBasedStatement(rateBasedStatement);
        }

        setManagedRuleGroupStatement(null);
        if (statement.managedRuleGroupStatement() != null) {
            ManagedRuleGroupStatementResource managedRuleGroupStatement = newSubresource(
                ManagedRuleGroupStatementResource.class);
            managedRuleGroupStatement.copyFrom(statement.managedRuleGroupStatement());
            setManagedRuleGroupStatement(managedRuleGroupStatement);
        }

        setRuleGroupReferenceStatement(null);
        if (statement.ruleGroupReferenceStatement() != null) {
            RuleGroupReferenceStatementResource ruleGroupReferenceStatement = newSubresource(
                RuleGroupReferenceStatementResource.class);
            ruleGroupReferenceStatement.copyFrom(statement.ruleGroupReferenceStatement());
            setRuleGroupReferenceStatement(ruleGroupReferenceStatement);
        }

        setHashCode(statement.hashCode());
    }

    Statement toStatement() {
        Statement.Builder builder = Statement.builder();

        if (getAndStatement() != null) {
            builder = builder.andStatement(getAndStatement().toAndStatement());
        }

        if (getNotStatement() != null) {
            builder = builder.notStatement(getNotStatement().toNotStatement());
        }

        if (getOrStatement() != null) {
            builder = builder.orStatement(getOrStatement().toOrStatement());
        }

        if (getByteMatchStatement() != null) {
            builder = builder.byteMatchStatement(getByteMatchStatement().toByteMatchStatement());
        }

        if (getGeoMatchStatement() != null) {
            builder = builder.geoMatchStatement(getGeoMatchStatement().toGeoMatchStatement());
        }

        if (getIpSetReferenceStatement() != null) {
            builder = builder.ipSetReferenceStatement(getIpSetReferenceStatement().toIpSetReferenceStatement());
        }

        if (getRegexPatternSetReferenceStatement() != null) {
            builder = builder.regexPatternSetReferenceStatement(getRegexPatternSetReferenceStatement().toRegexPatternSetReferenceStatement());
        }

        if (getSizeConstraintStatement() != null) {
            builder = builder.sizeConstraintStatement(getSizeConstraintStatement().toSizeConstraintStatement());
        }

        if (getSqliMatchStatement() != null) {
            builder = builder.sqliMatchStatement(getSqliMatchStatement().toSqliMatchStatement());
        }

        if (getXssMatchStatement() != null) {
            builder = builder.xssMatchStatement(getXssMatchStatement().toXssMatchStatement());
        }

        if (getRateBasedStatement() != null) {
            builder = builder.rateBasedStatement(getRateBasedStatement().toRateBasedStatement());
        }

        if (getManagedRuleGroupStatement() != null) {
            builder = builder.managedRuleGroupStatement(getManagedRuleGroupStatement().toManagedRuleGroupStatement());
        }

        if (getRuleGroupReferenceStatement() != null) {
            builder = builder.ruleGroupReferenceStatement(getRuleGroupReferenceStatement().toRuleGroupReferenceStatement());
        }

        return builder.build();
    }
}
