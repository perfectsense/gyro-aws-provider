/*
 * Copyright 2022, Brightspot.
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

import com.fasterxml.jackson.databind.JsonNode;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.lambda.model.AddLayerVersionPermissionRequest;

public class LayerPermission extends Diffable implements Copyable<AddLayerVersionPermissionRequest> {

    private String layerName;
    private Long versionNumber;
    private String organizationId;
    private String principal;
    private String statementId;
    private String action;
    private String revisionId;

    /**
     * The name of the layer.
     */
    @Required
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    /**
     * The version number of the layer.
     */
    @Updatable
    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Long versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * The AWS organization ID.
     */
    @Updatable
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
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
     * A unique statement identifier.
     */
    @Required
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
     * The revision ID of the layer.
     */
    @Updatable
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @Override
    public void copyFrom(AddLayerVersionPermissionRequest model) {
        setLayerName(model.layerName());
        setVersionNumber(model.versionNumber());
        setOrganizationId(model.organizationId());
        setPrincipal(model.principal());
        setStatementId(model.statementId());
        setAction(model.action());
        setRevisionId(model.revisionId());
    }

    @Override
    public String primaryKey() {
        return getStatementId();
    }

    AddLayerVersionPermissionRequest toAddLayerVersionPermissionRequest() {
        return AddLayerVersionPermissionRequest.builder()
            .layerName(getLayerName())
            .versionNumber(getVersionNumber())
            .organizationId(getOrganizationId())
            .principal(getPrincipal())
            .statementId(getStatementId())
            .action(getAction())
            .revisionId(getRevisionId())
            .build();
    }

    protected static AddLayerVersionPermissionRequest getAddLayerPermissionRequest(JsonNode statement) {
        AddLayerVersionPermissionRequest.Builder builder = AddLayerVersionPermissionRequest.builder();

        if (!statement.has("Sid")
            || !statement.has("Action")
            || !statement.has("Resource")) {
            throw new IllegalArgumentException("Invalid statement. 'Sid', 'Action' and 'Resource' are required fields.");
        }

        builder.statementId(statement.get("Sid").asText())
            .action(statement.get("Action").asText())
            .layerName(extractLayerName(statement.get("Resource").asText()))
            .versionNumber(extractVersionNumber(statement.get("Resource").asText()));

        if (statement.has("Principal")) {
            if (statement.get("Principal").has("AWS")) {
                builder.principal(statement.get("Principal").get("AWS").asText());
            } else if (statement.get("Principal").has("Service")) {
                builder.principal(statement.get("Principal").get("Service").asText());
            } else {
                throw new IllegalArgumentException("Invalid statement. 'Principal' must be either 'AWS' or 'Service'.");
            }
        }

        if (statement.has("PrincipalOrgID")) {
            builder.organizationId(statement.get("PrincipalOrgID").asText());
        }

        return builder.build();
    }

    private static String extractLayerName(String resourceArn) {
        String[] parts = resourceArn.split(":");
        return parts[6];
    }

    private static Long extractVersionNumber(String resourceArn) {
        String[] parts = resourceArn.split(":");
        return Long.parseLong(parts[7]);
    }

}
