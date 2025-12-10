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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.AddressField;
import software.amazon.awssdk.services.wafv2.model.EmailField;
import software.amazon.awssdk.services.wafv2.model.PasswordField;
import software.amazon.awssdk.services.wafv2.model.PhoneNumberField;
import software.amazon.awssdk.services.wafv2.model.RequestInspectionACFP;
import software.amazon.awssdk.services.wafv2.model.PayloadType;
import software.amazon.awssdk.services.wafv2.model.UsernameField;

public class RequestInspectionACFPResource extends Diffable implements Copyable<RequestInspectionACFP> {

    private String payloadType;
    private String usernameField;
    private String passwordField;
    private String emailField;
    private List<String> phoneNumberFields;
    private List<String> addressFields;

    /**
     * The payload type for the account creation requests inspected by the ACFP managed rule group.
     * For example, JSON or FORM_ENCODED.
     */
    @Required
    @ValidStrings({ "JSON", "FORM_ENCODED" })
    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * The name of the field in the request payload that contains the username.
     */
    public String getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(String usernameField) {
        this.usernameField = usernameField;
    }

    /**
     * The name of the field in the request payload that contains the password.
     */
    public String getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(String passwordField) {
        this.passwordField = passwordField;
    }

    /**
     * The name of the field in the request payload that contains the email address.
     */
    public String getEmailField() {
        return emailField;
    }

    public void setEmailField(String emailField) {
        this.emailField = emailField;
    }

    /**
     * A list of fields in the request payload that contain phone numbers.
     */
    public List<String> getPhoneNumberFields() {
        if (phoneNumberFields == null) {
            phoneNumberFields = new ArrayList<>();
        }
        return phoneNumberFields;
    }

    public void setPhoneNumberFields(List<String> phoneNumberFields) {
        this.phoneNumberFields = phoneNumberFields;
    }

    /**
     * A list of fields in the request payload that contain physical addresses.
     */
    public List<String> getAddressFields() {
        if (addressFields == null) {
            addressFields = new ArrayList<>();
        }
        return addressFields;
    }

    public void setAddressFields(List<String> addressFields) {
        this.addressFields = addressFields;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(RequestInspectionACFP requestInspectionACFP) {
        setPayloadType(requestInspectionACFP.payloadType() != null ? requestInspectionACFP.payloadType().toString() : null);

        setUsernameField(requestInspectionACFP.usernameField() != null
            ? requestInspectionACFP.usernameField().identifier()
            : null);

        setPasswordField(requestInspectionACFP.passwordField() != null
            ? requestInspectionACFP.passwordField().identifier()
            : null);

        setEmailField(requestInspectionACFP.emailField() != null
            ? requestInspectionACFP.emailField().identifier()
            : null);

        getPhoneNumberFields().clear();
        if (requestInspectionACFP.phoneNumberFields() != null) {
            requestInspectionACFP.phoneNumberFields().forEach(p ->
                getPhoneNumberFields().add(p.identifier())
            );
        }

        getAddressFields().clear();
        if (requestInspectionACFP.addressFields() != null) {
            requestInspectionACFP.addressFields().forEach(a ->
                getAddressFields().add(a.identifier())
            );
        }
    }

    RequestInspectionACFP toRequestInspectionACFP() {
        RequestInspectionACFP.Builder builder = RequestInspectionACFP.builder();

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

        if (getEmailField() != null) {
            builder.emailField(EmailField.builder()
                .identifier(getEmailField())
                .build());
        }

        if (!getPhoneNumberFields().isEmpty()) {
            List<PhoneNumberField> phoneFields = new ArrayList<>();
            for (String p : getPhoneNumberFields()) {
                phoneFields.add(PhoneNumberField.builder()
                    .identifier(p)
                    .build());
            }
            builder.phoneNumberFields(phoneFields);
        }

        if (!getAddressFields().isEmpty()) {
            List<AddressField> addrFields = new ArrayList<>();
            for (String a : getAddressFields()) {
                addrFields.add(AddressField.builder()
                    .identifier(a)
                    .build());
            }
            builder.addressFields(addrFields);
        }

        return builder.build();
    }
}
