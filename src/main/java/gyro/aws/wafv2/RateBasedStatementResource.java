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
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatement;

public class RateBasedStatementResource extends Diffable implements Copyable<RateBasedStatement> {

    private String aggregateKeyType;
    private Long limit;
    private StatementResource scopeDownStatement;
    private Set<RateBasedStatementCustomKeyResource> customKeys;
    private RateLimitForwardedIpConfigResource forwardedIpConfig;

    /**
     * The aggregate key type for the rate based statement. Defaults to ``IP``.
     */
    @ValidStrings({ "IP", "FORWARDED_IP", "CONSTANT", "CUSTOM_KEYS" })
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
     * The rate limit for the rate based statement.
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
     */
    public StatementResource getScopeDownStatement() {
        return scopeDownStatement;
    }

    public void setScopeDownStatement(StatementResource scopeDownStatement) {
        this.scopeDownStatement = scopeDownStatement;
    }

    /**
     * The list of custom key configs for the rate based statement.
     *
     * @subresource gyro.aws.wafv2.RateBasedStatementCustomKeyResource
     */
    @Updatable
    @CollectionMax(5)
    public Set<RateBasedStatementCustomKeyResource> getCustomKeys() {
        if (customKeys == null) {
            customKeys = new HashSet<>();
        }

        return customKeys;
    }

    public void setCustomKeys(Set<RateBasedStatementCustomKeyResource> customKeys) {
        this.customKeys = customKeys;
    }

    /**
     * The forwarded IP configuration for the rate based statement.
     *
     * @subresource gyro.aws.wafv2.RateLimitForwardedIpConfigResource
     */
    @Updatable
    public RateLimitForwardedIpConfigResource getForwardedIpConfig() {
        return forwardedIpConfig;
    }

    public void setForwardedIpConfig(RateLimitForwardedIpConfigResource forwardedIpConfig) {
        this.forwardedIpConfig = forwardedIpConfig;
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

        setCustomKeys(null);
        if (rateBasedStatement.customKeys() != null) {
            setCustomKeys(rateBasedStatement.customKeys().stream()
                .map(customKey -> {
                    RateBasedStatementCustomKeyResource customKeyResource = newSubresource(
                        RateBasedStatementCustomKeyResource.class);
                    customKeyResource.copyFrom(customKey);
                    return customKeyResource;
                }).collect(Collectors.toSet()));
        }

        setForwardedIpConfig(null);
        if (rateBasedStatement.forwardedIPConfig() != null) {
            RateLimitForwardedIpConfigResource forwardedIpConfig = newSubresource(RateLimitForwardedIpConfigResource.class);
            forwardedIpConfig.copyFrom(rateBasedStatement.forwardedIPConfig());
            setForwardedIpConfig(forwardedIpConfig);
        }
    }

    RateBasedStatement toRateBasedStatement() {
        RateBasedStatement.Builder builder = RateBasedStatement.builder()
            .aggregateKeyType(getAggregateKeyType())
            .limit(getLimit());

        if (getScopeDownStatement() != null) {
            builder = builder.scopeDownStatement(getScopeDownStatement().toStatement());
        }

        if (!getCustomKeys().isEmpty()) {
            builder.customKeys(getCustomKeys().stream()
                .map(RateBasedStatementCustomKeyResource::toRateBasedStatementCustomKey)
                .collect(Collectors.toList()));
        }

        if (getForwardedIpConfig() != null) {
            builder = builder.forwardedIPConfig(getForwardedIpConfig().toForwardedIPConfig());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getAggregateKeyType().equals("CUSTOM_KEYS") && getCustomKeys().isEmpty()) {
            errors.add(new ValidationError(
                this,
                "custom-keys",
                "'custom-keys' is required when 'aggregate-key-type' is set to 'CUSTOM_KEYS'."));
        } else if (!getAggregateKeyType().equals("CUSTOM_KEYS") && !getCustomKeys().isEmpty()) {
            errors.add(new ValidationError(
                this,
                "custom-keys",
                "'custom-keys' is not allowed when 'aggregate-key-type' is not set to 'CUSTOM_KEYS'."));
        }

        if (getForwardedIpConfig() == null) {
            if (getAggregateKeyType().equals("FORWARDED_IP")) {
                errors.add(new ValidationError(
                    this,
                    "forwarded-ip-config",
                    "'forwarded-ip-config' is required when 'aggregate-key-type' is set to 'FORWARDED_IP'."));
            } else if (getAggregateKeyType().equals("CUSTOM_KEYS")
                && getCustomKeys().stream().anyMatch(k -> k.getForwardedIp() != null)) {
                errors.add(new ValidationError(
                    this,
                    "forwarded-ip-config",
                    "'forwarded-ip-config' is required when 'aggregate-key-type' is set to 'CUSTOM_KEYS' and one of the custom keys have 'forwarded-ip' set."));
            }
        } else {
            if ((getAggregateKeyType().equals("CUSTOM_KEYS")
                && getCustomKeys().stream().noneMatch(k -> k.getForwardedIp() != null))
                || (!getAggregateKeyType().equals("CUSTOM_KEYS") && !getAggregateKeyType().equals("FORWARDED_IP"))) {
                errors.add(new ValidationError(
                    this,
                    "forwarded-ip-config",
                    "'forwarded-ip-config' is only allowed when either 'aggregate-key-type' is set to 'FORWARDED_IP' or \n "
                        + "'aggregate-key-type' is set to 'CUSTOM_KEYS' and one of the custom keys have 'forwarded-ip' set."));
            }
        }

        if (getAggregateKeyType().equals("CONSTANT") && getScopeDownStatement() == null) {
            errors.add(new ValidationError(
                this,
                "scope-down-statement",
                "'scope-down-statement' is required when 'aggregate-key-type' is set to 'CONSTANT'."));
        }

        return errors;
    }
}
