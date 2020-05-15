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

package gyro.aws.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.s3.model.Grantee;
import software.amazon.awssdk.services.s3.model.Type;

public class S3Grantee extends Diffable implements Copyable<Grantee> {

    private String displayName;
    private String id;
    private String uri;
    private String email;
    private Type type;

    /**
     * The display name of the grantee.
     */
    @ConflictsWith({"id", "uri", "email"})
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * The canonical user ID of the grantee.
     */
    @ConflictsWith({"display-name", "uri", "email"})
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The URI of the grantee group.
     */
    @ConflictsWith({"display-name", "id", "email"})
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * The email address of the grantee.
     */
    @ConflictsWith({"display-name", "id", "uri"})
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The type of the grantee. (Required)
     */
    @Required
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void copyFrom(Grantee model) {
        setDisplayName(model.displayName());
        setId(model.id());
        setUri(model.uri());
        setType(model.type());
        setEmail(model.emailAddress());
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getType());

        if (getDisplayName() != null) {
            sb.append(" ").append(getDisplayName());
        }

        if (getEmail() != null) {
            sb.append(" ").append(getEmail());
        }

        if (getId() != null) {
            sb.append(" ").append(getId());
        }

        if (getUri() != null) {
            sb.append(" ").append(getUri());
        }

        return sb.toString();
    }

    Grantee toGrantee() {
        return Grantee.builder().id(getId())
                .type(getType())
                .displayName(getDisplayName())
                .uri(getUri())
                .emailAddress(getEmail())
                .type(getType())
                .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("id") && !configuredFields.contains("display-name")
                && !configuredFields.contains("uri") && !configuredFields.contains("email")) {
            errors.add(new ValidationError(this, null, "At least one of 'display-name', 'id', 'email' or 'uri' is required"));
        }

        return errors;
    }
}
