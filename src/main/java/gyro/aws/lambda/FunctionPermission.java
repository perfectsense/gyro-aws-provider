/*
 * Copyright 2025, Brightspot.
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

package gyro.aws.lambda;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.lambda.model.AddPermissionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionUrlAuthType;

public class FunctionPermission extends Diffable implements Copyable<AddPermissionRequest> {
    private static final Pattern ARN_FORMAT = Pattern.compile(
        "^arn:aws:lambda:(?<region>[a-zA-Z0-9-]*):(?<ownerId>[0-9-]*):function:(?<name>[a-zA-Z0-9.:-]*)$");

    private String statementId;
    private String action;
    private String principal;
    private String sourceArn;
    private String sourceAccount;
    private String eventSourceToken;
    private String revisionId;
    private String principalOrgId;
    private FunctionUrlAuthType functionUrlAuthType;

    // Read-only
    private String functionName;
    private String qualifier;

    /**
     * A unique statement identifier.
     */
    @Required
    @Regex("[a-zA-Z0-9-_]+")
    public String getStatementId() {
        return statementId;
    }

    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    /**
     * The action that the principal can perform.
     */
    @Updatable
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * The principal who is getting this permission.
     */
    @Updatable
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * The ARN of the source that is invoking the function.
     */
    @Updatable
    public String getSourceArn() {
        return sourceArn;
    }

    public void setSourceArn(String sourceArn) {
        this.sourceArn = sourceArn;
    }

    /**
     * The AWS account ID of the source that is invoking the function.
     */
    @Updatable
    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    /**
     * The token that must be present in the request.
     */
    @Updatable
    public String getEventSourceToken() {
        return eventSourceToken;
    }

    public void setEventSourceToken(String eventSourceToken) {
        this.eventSourceToken = eventSourceToken;
    }

    /**
     * The revision ID of the function.
     */
    @Updatable
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * The organization ID of the principal.
     */
    @Updatable
    public String getPrincipalOrgId() {
        return principalOrgId;
    }

    public void setPrincipalOrgId(String principalOrgId) {
        this.principalOrgId = principalOrgId;
    }

    /**
     * The type of authentication to use.
     */
    @Updatable
    @ValidStrings({ "NONE", "AWS_IAM" })
    public FunctionUrlAuthType getFunctionUrlAuthType() {
        return functionUrlAuthType;
    }

    public void setFunctionUrlAuthType(FunctionUrlAuthType functionUrlAuthType) {
        this.functionUrlAuthType = functionUrlAuthType;
    }

    /**
     * The name of the Lambda function. Inherited from the parent.
     */
    @Output
    public String getFunctionName() {
        if (functionName == null) {
            if (parent() instanceof FunctionResource) {
                functionName = ((FunctionResource) parent()).getName();

            } else if (parent() instanceof FunctionVersionResource) {
                functionName = ((FunctionVersionResource) parent()).getFunctionName();
            }
        }

        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The version or alias of the function. Inherited from the parent.
     */
    @Output
    public String getQualifier() {
        if (qualifier == null && parent() instanceof FunctionVersionResource) {
            qualifier = ((FunctionVersionResource) parent()).getVersion();
        }

        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public String primaryKey() {
        return getStatementId();
    }

    @Override
    public void copyFrom(AddPermissionRequest model) {
        setFunctionName(model.functionName());
        setStatementId(model.statementId());
        setAction(model.action());
        setPrincipal(model.principal());
        setSourceArn(model.sourceArn());
        setSourceAccount(model.sourceAccount());
        setEventSourceToken(model.eventSourceToken());
        setQualifier(model.qualifier());
        setRevisionId(model.revisionId());
        setPrincipalOrgId(model.principalOrgID());
        setFunctionUrlAuthType(model.functionUrlAuthType());
    }

    AddPermissionRequest toAddPermissionRequest() {
        return AddPermissionRequest.builder()
            .functionName(getFunctionName())
            .statementId(getStatementId())
            .action(getAction())
            .principal(getPrincipal())
            .sourceArn(getSourceArn())
            .sourceAccount(getSourceAccount())
            .eventSourceToken(getEventSourceToken())
            .qualifier(getQualifier())
            .revisionId(getRevisionId())
            .principalOrgID(getPrincipalOrgId())
            .functionUrlAuthType(getFunctionUrlAuthType())
            .build();
    }

    protected static AddPermissionRequest getAddPermissionRequest(JsonNode statement) {
        AddPermissionRequest.Builder builder = AddPermissionRequest.builder();

        if (!statement.has("Sid")
            || !statement.has("Action")
            || !statement.has("Resource")
            || !statement.has("Principal")) {
            throw new IllegalArgumentException(
                "Invalid statement. 'Sid', 'Action', 'Resource' and 'Principal' are required fields.");
        }

        if (!statement.get("Sid").asText().matches("[a-zA-Z0-9-_]+")) {
            // Ignore statements that don't match the provided regex
            // These are AWS managed permissions that cannot be configured
            return null;
        }

        builder.statementId(statement.get("Sid").asText())
            .action(statement.get("Action").asText());

        Matcher matcher = ARN_FORMAT.matcher(statement.get("Resource").asText());

        if (matcher.matches()) {
            String name = matcher.group("name");
            String qualifier = null;

            if (StringUtils.isNotBlank(name) && name.contains(":")) {
                String[] functionSplit = name.split(":");
                name = functionSplit[0];
                qualifier = functionSplit[1];
            }

            builder.qualifier(qualifier);
            builder.functionName(name);

        } else {
            builder.functionName(statement.get("Resource").asText());
        }

        if (statement.get("Principal").has("Service")) {
            builder.principal(statement.get("Principal").get("Service").asText());
        } else if (statement.get("Principal").has("AWS")) {
            builder.principal(statement.get("Principal").get("AWS").asText());
        } else {
            throw new IllegalArgumentException(
                "Invalid statement. 'Principal' must have either 'Service' or 'AWS' field.");
        }

        if (statement.has("RevisionId")) {
            builder.revisionId(statement.get("RevisionId").asText());
        }

        if (statement.has("Condition")) {
            JsonNode condition = statement.get("Condition");
            Iterator<String> fieldNames = condition.fieldNames();

            while (fieldNames.hasNext()) {
                String conditionKey = fieldNames.next();
                JsonNode conditionValue = condition.get(conditionKey);

                if (conditionKey.equals("ArnLike") && conditionValue.has("AWS:SourceArn")) {
                    builder.sourceArn(conditionValue.get("AWS:SourceArn").asText());
                } else if (conditionKey.equals("StringEquals")) {
                    if (conditionValue.has("AWS:SourceAccount")) {
                        builder.sourceAccount(conditionValue.get("AWS:SourceAccount").asText());
                    } else if (conditionValue.has("lambda:EventSourceToken")) {
                        builder.eventSourceToken(conditionValue.get("lambda:EventSourceToken").asText());
                    } else if (conditionValue.has("aws:PrincipalOrgID")) {
                        builder.principalOrgID(conditionValue.get("aws:PrincipalOrgID").asText());
                    } else if (conditionValue.has("lambda:FunctionUrlAuthType")) {
                        builder.functionUrlAuthType(FunctionUrlAuthType.fromValue(
                            conditionValue.get("lambda:FunctionUrlAuthType").asText()));
                    }
                }
            }
        }

        return builder.build();
    }
}
