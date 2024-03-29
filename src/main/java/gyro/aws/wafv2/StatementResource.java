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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.Statement;

public class StatementResource extends Diffable implements Copyable<Statement> {

    private AndStatementResource andStatement;
    private NotStatementResource notStatement;
    private OrStatementResource orStatement;
    private ByteMatchStatementResource byteMatchStatement;
    private GeoMatchStatementResource geoMatchStatement;
    private IpSetReferenceStatementResource ipSetReferenceStatement;
    private RegexPatternSetReferenceStatementResource regexPatternSetReferenceStatement;
    private RegexMatchStatementResource regexMatchStatement;
    private SizeConstraintStatementResource sizeConstraintStatement;
    private SqliMatchStatementResource sqliMatchStatement;
    private XssMatchStatementResource xssMatchStatement;
    private LabelMatchStatementResource labelMatchStatement;
    private RateBasedStatementResource rateBasedStatement;
    private ManagedRuleGroupStatementResource managedRuleGroupStatement;
    private RuleGroupReferenceStatementResource ruleGroupReferenceStatement;

    /**
     * And statement configuration.
     *
     * @subresource gyro.aws.wafv2.AndStatementResource
     */
    public AndStatementResource getAndStatement() {
        return andStatement;
    }

    public void setAndStatement(AndStatementResource andStatement) {
        this.andStatement = andStatement;
    }

    /**
     * Not statement configuration.
     *
     * @subresource gyro.aws.wafv2.NotStatementResource
     */
    public NotStatementResource getNotStatement() {
        return notStatement;
    }

    public void setNotStatement(NotStatementResource notStatement) {
        this.notStatement = notStatement;
    }

    /**
     * Or statement configuration.
     *
     * @subresource gyro.aws.wafv2.OrStatementResource
     */
    public OrStatementResource getOrStatement() {
        return orStatement;
    }

    public void setOrStatement(OrStatementResource orStatement) {
        this.orStatement = orStatement;
    }

    /**
     * Byte Match statement configuration.
     *
     * @subresource gyro.aws.wafv2.ByteMatchStatementResource
     */
    public ByteMatchStatementResource getByteMatchStatement() {
        return byteMatchStatement;
    }

    public void setByteMatchStatement(ByteMatchStatementResource byteMatchStatement) {
        this.byteMatchStatement = byteMatchStatement;
    }

    /**
     * Geo statement configuration.
     *
     * @subresource gyro.aws.wafv2.GeoMatchStatementResource
     */
    public GeoMatchStatementResource getGeoMatchStatement() {
        return geoMatchStatement;
    }

    public void setGeoMatchStatement(GeoMatchStatementResource geoMatchStatement) {
        this.geoMatchStatement = geoMatchStatement;
    }

    /**
     * IP set reference statement configuration.
     *
     * @subresource gyro.aws.wafv2.IpSetReferenceStatementResource
     */
    public IpSetReferenceStatementResource getIpSetReferenceStatement() {
        return ipSetReferenceStatement;
    }

    public void setIpSetReferenceStatement(IpSetReferenceStatementResource ipSetReferenceStatement) {
        this.ipSetReferenceStatement = ipSetReferenceStatement;
    }

    /**
     * Regex pattern reference statement configuration.
     *
     * @subresource gyro.aws.wafv2.RegexPatternReferenceStatementResource
     */
    public RegexPatternSetReferenceStatementResource getRegexPatternSetReferenceStatement() {
        return regexPatternSetReferenceStatement;
    }

    public void setRegexPatternSetReferenceStatement(RegexPatternSetReferenceStatementResource regexPatternSetReferenceStatement) {
        this.regexPatternSetReferenceStatement = regexPatternSetReferenceStatement;
    }

    /**
     * Regex match statement configuration.
     *
     * @subresource gyro.aws.wafv2.RegexMatchStatementResource
     */
    public RegexMatchStatementResource getRegexMatchStatement() {
        return regexMatchStatement;
    }

    public void setRegexMatchStatement(RegexMatchStatementResource regexMatchStatement) {
        this.regexMatchStatement = regexMatchStatement;
    }

    /**
     * Size constraint statement configuration.
     *
     * @subresource gyro.aws.wafv2.SizeConstraintStatementResource
     */
    public SizeConstraintStatementResource getSizeConstraintStatement() {
        return sizeConstraintStatement;
    }

    public void setSizeConstraintStatement(SizeConstraintStatementResource sizeConstraintStatement) {
        this.sizeConstraintStatement = sizeConstraintStatement;
    }

    /**
     * Sql Injection statement configuration.
     *
     * @subresource gyro.aws.wafv2.SqliStatementResource
     */
    public SqliMatchStatementResource getSqliMatchStatement() {
        return sqliMatchStatement;
    }

    public void setSqliMatchStatement(SqliMatchStatementResource sqliMatchStatement) {
        this.sqliMatchStatement = sqliMatchStatement;
    }

    /**
     * Xss match statement configuration.
     *
     * @subresource gyro.aws.wafv2.XssMatchStatementResource
     */
    public XssMatchStatementResource getXssMatchStatement() {
        return xssMatchStatement;
    }

    public void setXssMatchStatement(XssMatchStatementResource xssMatchStatement) {
        this.xssMatchStatement = xssMatchStatement;
    }

    /**
     * Label match statement configuration.
     *
     * @subresource gyro.aws.wafv2.LabelMatchStatementResource
     */
    public LabelMatchStatementResource getLabelMatchStatement() {
        return labelMatchStatement;
    }

    public void setLabelMatchStatement(LabelMatchStatementResource labelMatchStatement) {
        this.labelMatchStatement = labelMatchStatement;
    }

    /**
     * Rate based statement configuration.
     *
     * @subresource gyro.aws.wafv2.RateBasedStatementResource
     */
    public RateBasedStatementResource getRateBasedStatement() {
        return rateBasedStatement;
    }

    public void setRateBasedStatement(RateBasedStatementResource rateBasedStatement) {
        this.rateBasedStatement = rateBasedStatement;
    }

    /**
     * Managed rule group statement configuration.
     *
     * @subresource gyro.aws.wafv2.ManagedRuleGroupStatementResource
     */
    public ManagedRuleGroupStatementResource getManagedRuleGroupStatement() {
        return managedRuleGroupStatement;
    }

    public void setManagedRuleGroupStatement(ManagedRuleGroupStatementResource managedRuleGroupStatement) {
        this.managedRuleGroupStatement = managedRuleGroupStatement;
    }

    /**
     * Rule group reference statement configuration.
     *
     * @subresource gyro.aws.wafv2.RuleGroupReferenceStatementResource
     */
    public RuleGroupReferenceStatementResource getRuleGroupReferenceStatement() {
        return ruleGroupReferenceStatement;
    }

    @Override
    public String primaryKey() {
        return String.format("'%s' containing [%s]", findStatementType(), findStatementDetailPrimaryKey());
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

        setRegexMatchStatement(null);
        if (statement.regexMatchStatement() != null) {
            RegexMatchStatementResource regexMatchStatement = newSubresource(RegexMatchStatementResource.class);
            regexMatchStatement.copyFrom(statement.regexMatchStatement());
            setRegexMatchStatement(regexMatchStatement);
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

        setLabelMatchStatement(null);
        if (statement.labelMatchStatement() != null) {
            LabelMatchStatementResource labelMatchStatement = newSubresource(LabelMatchStatementResource.class);
            labelMatchStatement.copyFrom(statement.labelMatchStatement());
            setLabelMatchStatement(labelMatchStatement);
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
    }

    Statement toStatement() {
        Statement.Builder builder = Statement.builder();

        if (getAndStatement() != null) {
            builder = builder.andStatement(getAndStatement().toAndStatement());
        } else if (getNotStatement() != null) {
            builder = builder.notStatement(getNotStatement().toNotStatement());
        } else if (getOrStatement() != null) {
            builder = builder.orStatement(getOrStatement().toOrStatement());
        } else if (getByteMatchStatement() != null) {
            builder = builder.byteMatchStatement(getByteMatchStatement().toByteMatchStatement());
        } else if (getGeoMatchStatement() != null) {
            builder = builder.geoMatchStatement(getGeoMatchStatement().toGeoMatchStatement());
        } else if (getIpSetReferenceStatement() != null) {
            builder = builder.ipSetReferenceStatement(getIpSetReferenceStatement().toIpSetReferenceStatement());
        } else if (getRegexPatternSetReferenceStatement() != null) {
            builder = builder.regexPatternSetReferenceStatement(getRegexPatternSetReferenceStatement().toRegexPatternSetReferenceStatement());
        } else if (getRegexMatchStatement() != null) {
            builder = builder.regexMatchStatement(getRegexMatchStatement().toRegexMatchStatement());
        } else if (getSizeConstraintStatement() != null) {
            builder = builder.sizeConstraintStatement(getSizeConstraintStatement().toSizeConstraintStatement());
        } else if (getSqliMatchStatement() != null) {
            builder = builder.sqliMatchStatement(getSqliMatchStatement().toSqliMatchStatement());
        } else if (getXssMatchStatement() != null) {
            builder = builder.xssMatchStatement(getXssMatchStatement().toXssMatchStatement());
        } else if (getRateBasedStatement() != null) {
            builder = builder.rateBasedStatement(getRateBasedStatement().toRateBasedStatement());
        } else if (getLabelMatchStatement() !=null) {
            builder = builder.labelMatchStatement(getLabelMatchStatement().toLabelMatchStatement());
        } else if (getManagedRuleGroupStatement() != null) {
            builder = builder.managedRuleGroupStatement(getManagedRuleGroupStatement().toManagedRuleGroupStatement());
        } else if (getRuleGroupReferenceStatement() != null) {
            builder = builder.ruleGroupReferenceStatement(getRuleGroupReferenceStatement().toRuleGroupReferenceStatement());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        long count = Stream.of(
            getAndStatement(),
            getOrStatement(),
            getNotStatement(),
            getByteMatchStatement(),
            getGeoMatchStatement(),
            getIpSetReferenceStatement(),
            getRegexPatternSetReferenceStatement(),
            getRegexMatchStatement(),
            getSizeConstraintStatement(),
            getSqliMatchStatement(),
            getXssMatchStatement(),
            getLabelMatchStatement(),
            getRateBasedStatement(),
            getManagedRuleGroupStatement(),
            getRuleGroupReferenceStatement())
            .filter(Objects::nonNull)
            .count();

        if (count != 1) {

            errors.add(new ValidationError(
                this,
                null,
                "One and only one of [ 'and-statement', 'not-statement', 'or-statement', 'byte-match-statement',"
                    + "'geo-match-statement', 'ip-set-reference-statement', 'regex-pattern-set-reference-statement',"
                    + "'regex-match-statement', 'size-constraint-statement', 'sqli-match-statement', 'xss-match-statement',"
                    + "'rate-based-statement', 'label-match-statement', 'managed-rule-group-statement' or 'rule-group-reference-statement' ] "
                    + "is required"));
        }

        return errors;
    }

    private String findStatementType() {
        String type = "";

        if (getAndStatement() != null) {
            type = "and";
        } else if (getNotStatement() != null) {
            type = "not";
        } else if (getOrStatement() != null) {
            type = "or";
        } else if (getByteMatchStatement() != null) {
            type = "byte match";
        } else if (getGeoMatchStatement() != null) {
            type = "geo match";
        } else if (getIpSetReferenceStatement() != null) {
            type = "ip set reference";
        } else if (getRegexPatternSetReferenceStatement() != null) {
            type = "regex pattern reference";
        } else if (getRegexMatchStatement() != null) {
            type = "regex match";
        } else if (getSizeConstraintStatement() != null) {
            type = "size constraint";
        } else if (getSqliMatchStatement() != null) {
            type = "sql injection match";
        } else if (getXssMatchStatement() != null) {
            type = "xss match";
        } else if (getLabelMatchStatement() != null) {
            type = "label match";
        } else if (getRateBasedStatement() != null) {
            type = "rate based";
        } else if (getManagedRuleGroupStatement() != null) {
            type = "managed rule group";
        } else if (getRuleGroupReferenceStatement() != null) {
            type = "rule group reference";
        }

        return type;
    }

    private String findStatementDetailPrimaryKey() {
        String key = "";

        if (getAndStatement() != null) {
            key = getAndStatement().primaryKey();
        } else if (getNotStatement() != null) {
            key = getNotStatement().primaryKey();
        } else if (getOrStatement() != null) {
            key = getOrStatement().primaryKey();
        } else if (getByteMatchStatement() != null) {
            key = getByteMatchStatement().primaryKey();
        } else if (getGeoMatchStatement() != null) {
            key = getGeoMatchStatement().primaryKey();
        } else if (getIpSetReferenceStatement() != null) {
            key = getIpSetReferenceStatement().primaryKey();
        } else if (getRegexPatternSetReferenceStatement() != null) {
            key = getRegexPatternSetReferenceStatement().primaryKey();
        } else if (getRegexMatchStatement() != null) {
            key = getRegexMatchStatement().primaryKey();
        } else if (getSizeConstraintStatement() != null) {
            key = getSizeConstraintStatement().primaryKey();
        } else if (getSqliMatchStatement() != null) {
            key = getSqliMatchStatement().primaryKey();
        } else if (getXssMatchStatement() != null) {
            key = getXssMatchStatement().primaryKey();
        } else if (getLabelMatchStatement() != null) {
            key = getLabelMatchStatement().primaryKey();
        } else if (getRateBasedStatement() != null) {
            key = getRateBasedStatement().primaryKey();
        } else if (getManagedRuleGroupStatement() != null) {
            key = getManagedRuleGroupStatement().primaryKey();
        } else if (getRuleGroupReferenceStatement() != null) {
            key = getRuleGroupReferenceStatement().primaryKey();
        }

        return key;
    }

    boolean isRuleGroupReferenceStatement() {
        return getRuleGroupReferenceStatement() != null || getManagedRuleGroupStatement() != null;
    }

    public void setRuleGroupReferenceStatement(RuleGroupReferenceStatementResource ruleGroupReferenceStatement) {
        this.ruleGroupReferenceStatement = ruleGroupReferenceStatement;
    }

    boolean isRuleRateBased() {
        return getRateBasedStatement() != null;
    }
}
