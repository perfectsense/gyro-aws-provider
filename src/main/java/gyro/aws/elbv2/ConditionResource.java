/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.diff.Update;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     condition
 *         field: "host-header"
 *
 *         host-header-config
 *             values: ["www.example.net"]
 *         end
 *     end
 */
public class ConditionResource extends AwsResource implements Copyable<RuleCondition> {

    private String field;
    private HostHeaderConditionConfiguration hostHeaderConfig;
    private HttpHeaderConditionConfiguration httpHeaderConfig;
    private HttpRequestMethodConditionConfiguration httpRequestMethodConfig;
    private PathPatternConditionConfiguration pathPatternConfig;
    private QueryStringConditionConfiguration queryStringConfig;
    private SourceIpConditionConfiguration sourceIpConfig;

    /**
     *  Condition field name.
     */
    @Required
    @Updatable
    @ValidStrings({ "http-header", "http-request-method", "host-header", "path-pattern", "query-string", "source-ip" })
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * The information for a host header condition.
     */
    @Updatable
    @ConflictsWith({
        "http-header-config",
        "http-request-method-config",
        "path-pattern-config",
        "query-string-config",
        "source-ip-config"
    })
    public HostHeaderConditionConfiguration getHostHeaderConfig() {
        return hostHeaderConfig;
    }

    public void setHostHeaderConfig(HostHeaderConditionConfiguration hostHeaderConfig) {
        this.hostHeaderConfig = hostHeaderConfig;
    }

    /**
     * The information for an HTTP header condition.
     */
    @Updatable
    @ConflictsWith({
        "host-header-config",
        "http-request-method-config",
        "path-pattern-config",
        "query-string-config",
        "source-ip-config"
    })
    public HttpHeaderConditionConfiguration getHttpHeaderConfig() {
        return httpHeaderConfig;
    }

    public void setHttpHeaderConfig(HttpHeaderConditionConfiguration httpHeaderConfig) {
        this.httpHeaderConfig = httpHeaderConfig;
    }

    /**
     * The information for an HTTP method condition.
     */
    @Updatable
    @ConflictsWith({
        "host-header-config",
        "http-header-config",
        "path-pattern-config",
        "query-string-config",
        "source-ip-config"
    })
    public HttpRequestMethodConditionConfiguration getHttpRequestMethodConfig() {
        return httpRequestMethodConfig;
    }

    public void setHttpRequestMethodConfig(HttpRequestMethodConditionConfiguration httpRequestMethodConfig) {
        this.httpRequestMethodConfig = httpRequestMethodConfig;
    }

    /**
     * The information for a path pattern condition.
     */
    @Updatable
    @ConflictsWith({
        "host-header-config",
        "http-header-config",
        "http-request-method-config",
        "query-string-config",
        "source-ip-config"
    })
    public PathPatternConditionConfiguration getPathPatternConfig() {
        return pathPatternConfig;
    }

    public void setPathPatternConfig(PathPatternConditionConfiguration pathPatternConfig) {
        this.pathPatternConfig = pathPatternConfig;
    }

    /**
     * The information for a query string condition.
     */
    @Updatable
    @ConflictsWith({
        "host-header-config",
        "http-header-config",
        "http-request-method-config",
        "path-pattern-config",
        "source-ip-config"
    })
    public QueryStringConditionConfiguration getQueryStringConfig() {
        return queryStringConfig;
    }

    public void setQueryStringConfig(QueryStringConditionConfiguration queryStringConfig) {
        this.queryStringConfig = queryStringConfig;
    }

    /**
     * The information for a source IP condition.
     */
    @Updatable
    @ConflictsWith({
        "host-header-config",
        "http-header-config",
        "http-request-method-config",
        "path-pattern-config",
        "query-string-config"
    })
    public SourceIpConditionConfiguration getSourceIpConfig() {
        return sourceIpConfig;
    }

    public void setSourceIpConfig(SourceIpConditionConfiguration sourceIpConfig) {
        this.sourceIpConfig = sourceIpConfig;
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getField(), getHostHeaderConfig() == null ? (
            getHttpHeaderConfig() == null ? (getHttpRequestMethodConfig() == null
                ? (getPathPatternConfig() == null ? (
                getQueryStringConfig() == null ? (getSourceIpConfig() == null ? "" : getSourceIpConfig().getValues())
                    : getQueryStringConfig().getKeyValuePairs()) : getPathPatternConfig().getValues())
                : getHttpRequestMethodConfig().getValues())
                : getHttpHeaderConfig().getValues()) : getHostHeaderConfig().getValues());
    }

    @Override
    public void copyFrom(RuleCondition ruleCondition) {
        setField(ruleCondition.field());

        if (ruleCondition.hostHeaderConfig() != null) {
            HostHeaderConditionConfiguration config = newSubresource(HostHeaderConditionConfiguration.class);
            config.copyFrom(ruleCondition.hostHeaderConfig());
            setHostHeaderConfig(config);
        }

        if (ruleCondition.httpHeaderConfig() != null) {
            HttpHeaderConditionConfiguration config = newSubresource(HttpHeaderConditionConfiguration.class);
            config.copyFrom(ruleCondition.httpHeaderConfig());
            setHttpHeaderConfig(config);
        }

        if (ruleCondition.httpRequestMethodConfig() != null) {
            HttpRequestMethodConditionConfiguration config = newSubresource(HttpRequestMethodConditionConfiguration.class);
            config.copyFrom(ruleCondition.httpRequestMethodConfig());
            setHttpRequestMethodConfig(config);
        }

        if (ruleCondition.pathPatternConfig() != null) {
            PathPatternConditionConfiguration config = newSubresource(PathPatternConditionConfiguration.class);
            config.copyFrom(ruleCondition.pathPatternConfig());
            setPathPatternConfig(config);
        }

        if (ruleCondition.queryStringConfig() != null) {
            QueryStringConditionConfiguration config = newSubresource(QueryStringConditionConfiguration.class);
            config.copyFrom(ruleCondition.queryStringConfig());
            setQueryStringConfig(config);
        }

        if (ruleCondition.sourceIpConfig() != null) {
            SourceIpConditionConfiguration config = newSubresource(SourceIpConditionConfiguration.class);
            config.copyFrom(ruleCondition.sourceIpConfig());
            setSourceIpConfig(config);
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Create) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.createCondition(this);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        if (DiffableInternals.getChange(parentResource()) instanceof Update) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.updateCondition();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Delete) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.deleteCondition(this);
    }

    public RuleCondition toCondition() {
        return RuleCondition.builder()
            .field(getField())
            .hostHeaderConfig(getHostHeaderConfig() == null ?
                null : getHostHeaderConfig().toHostHeaderConditionConfig())
            .httpHeaderConfig(getHttpHeaderConfig() == null ?
                null : getHttpHeaderConfig().toHttpHeaderConditionConfig())
            .httpRequestMethodConfig(getHttpRequestMethodConfig() == null ? null
                : getHttpRequestMethodConfig().toHttpRequestMethodConditionConfig())
            .pathPatternConfig(getPathPatternConfig() == null ?
                null : getPathPatternConfig().toPathPatternConditionConfig())
            .queryStringConfig(getQueryStringConfig() == null ?
                null : getQueryStringConfig().toQueryStringConditionConfig())
            .sourceIpConfig(getSourceIpConfig() == null ? null : getSourceIpConfig().toSourceIpConditionConfig())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        if (getHostHeaderConfig() == null && getHttpHeaderConfig() == null
            && getHttpRequestMethodConfig() == null && getPathPatternConfig() == null && getQueryStringConfig() == null
            && getSourceIpConfig() == null) {
            errors.add(new ValidationError(
                this,
                null,
                "At least one of 'host-header-config' or 'http-header-config' or 'http-request-method-config' or 'path-pattern-config' or 'query-string-config' or 'source-ip-config' is required."));
        }

        if (getHttpHeaderConfig() != null && !getField().equals("http-header")) {
            errors.add(new ValidationError(
                this,
                null,
                "'http-header-config' can only be set if 'field' is set to 'http-header'"));
        }

        if (getHttpRequestMethodConfig() != null && !getField().equals("http-request-method")) {
            errors.add(new ValidationError(
                this,
                null,
                "'http-request-method-config' can only be set if 'field' is set to 'http-header'"));
        }

        if (getHostHeaderConfig() != null && !getField().equals("host-header")) {
            errors.add(new ValidationError(
                this,
                null,
                "'host-header-config' can only be set if 'field' is set to 'host-header'"));
        }

        if (getPathPatternConfig() != null && !getField().equals("path-pattern")) {
            errors.add(new ValidationError(
                this,
                null,
                "'path-pattern-config' can only be set if 'field' is set to 'path-pattern'"));
        }

        if (getQueryStringConfig() != null && !getField().equals("query-string")) {
            errors.add(new ValidationError(
                this,
                null,
                "'query-string-config' can only be set if 'field' is set to 'query-string'"));
        }

        if (getSourceIpConfig() != null && !getField().equals("source-ip")) {
            errors.add(new ValidationError(
                this,
                null,
                "'source-ip-config' can only be set if 'field' is set to 'source-ip'"));
        }

        return errors;
    }
}
