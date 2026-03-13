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

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.PasswordField;
import software.amazon.awssdk.services.wafv2.model.RequestInspection;
import software.amazon.awssdk.services.wafv2.model.PayloadType;
import software.amazon.awssdk.services.wafv2.model.UsernameField;

public class RequestInspectionResource extends Diffable implements Copyable<RequestInspection> {

    private String payloadType;
    private String usernameField;
    private String passwordField;

    /**
     * Payload type for login requests.
     */
    @Required
    @Updatable
    @ValidStrings({ "JSON", "FORM_ENCODED" })
    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * Name of the field in the request body that contains the username.
     */
    @Required
    @Updatable
    public String getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(String usernameField) {
        this.usernameField = usernameField;
    }

    /**
     * Name of the field in the request body that contains the password.
     */
    @Required
    @Updatable
    public String getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(String passwordField) {
        this.passwordField = passwordField;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(RequestInspection requestInspection) {
        setPayloadType(requestInspection.payloadType() != null ? requestInspection.payloadType().toString() : null);

        setUsernameField(requestInspection.usernameField() != null
            ? requestInspection.usernameField().identifier()
            : null);

        setPasswordField(requestInspection.passwordField() != null
            ? requestInspection.passwordField().identifier()
            : null);
    }

    RequestInspection toRequestInspection() {
        RequestInspection.Builder builder = RequestInspection.builder();

        if (getPayloadType() != null) {
            builder.payloadType(PayloadType.fromValue(getPayloadType()));
        }
        if (getUsernameField() != null) {
            builder.usernameField(UsernameField.builder()
                .identifier(getUsernameField())
                .build());
        }
        if (getPasswordField() != null) {
            builder.passwordField(PasswordField.builder()
                .identifier(getPasswordField())
                .build());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getUsernameField() != null && getUsernameField().length() > 512) {
            errors.add(new ValidationError(
                this,
                "username-field",
                "The param 'username-field' must not exceed 512 characters in length."));
        }
        if (getPasswordField() != null && getPasswordField().length() > 512) {
            errors.add(new ValidationError(
                this,
                "password-field",
                "The param 'password-field' must not exceed 512 characters in length."));
        }

        return errors;
    }
}
