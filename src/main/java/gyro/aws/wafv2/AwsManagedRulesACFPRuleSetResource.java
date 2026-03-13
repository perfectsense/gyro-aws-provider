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
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.AWSManagedRulesACFPRuleSet;

public class AwsManagedRulesACFPRuleSetResource extends Diffable implements Copyable<AWSManagedRulesACFPRuleSet> {

    private String creationPath;
    private String registrationPagePath;
    private Boolean enableRegexInPath;
    private RequestInspectionACFPResource requestInspection;
    private ResponseInspectionResource responseInspection;

    /**
     * Path of the account registration endpoint for your application.
     */
    @Required
    @Updatable
    public String getRegistrationPagePath() {
        return registrationPagePath;
    }

    public void setRegistrationPagePath(String registrationPagePath) {
        this.registrationPagePath = registrationPagePath;
    }

    /**
     * Path of the account creation endpoint for your application.
     */
    @Required
    @Updatable
    public String getCreationPath() {
        return creationPath;
    }

    public void setCreationPath(String creationPath) {
        this.creationPath = creationPath;
    }

    /**
     * Whether regex is allowed in the registration and creation paths.
     */
    @Updatable
    public Boolean getEnableRegexInPath() {
        return enableRegexInPath;
    }

    public void setEnableRegexInPath(Boolean enableRegexInPath) {
        this.enableRegexInPath = enableRegexInPath;
    }

    /**
     * Criteria for inspecting account creation requests.
     *
     * @subresource gyro.aws.wafv2.RequestInspectionACFPResource
     */
    @Required
    @Updatable
    public RequestInspectionACFPResource getRequestInspection() {
        return requestInspection;
    }

    public void setRequestInspection(RequestInspectionACFPResource requestInspection) {
        this.requestInspection = requestInspection;
    }

    /**
     * Criteria for inspecting responses to account creation requests.
     *
     * @subresource gyro.aws.wafv2.ResponseInspectionResource
     */
    @Updatable
    public ResponseInspectionResource getResponseInspection() {
        return responseInspection;
    }

    public void setResponseInspection(ResponseInspectionResource responseInspection) {
        this.responseInspection = responseInspection;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AWSManagedRulesACFPRuleSet awsManagedRulesACFPRuleSet) {
        setCreationPath(awsManagedRulesACFPRuleSet.creationPath());
        setRegistrationPagePath(awsManagedRulesACFPRuleSet.registrationPagePath());
        setEnableRegexInPath(awsManagedRulesACFPRuleSet.enableRegexInPath());

        setRequestInspection(null);
        if (awsManagedRulesACFPRuleSet.requestInspection() != null) {
            RequestInspectionACFPResource requestInspectionACFPResource = newSubresource(RequestInspectionACFPResource.class);
            requestInspectionACFPResource.copyFrom(awsManagedRulesACFPRuleSet.requestInspection());
            setRequestInspection(requestInspectionACFPResource);
        }

        setResponseInspection(null);
        if (awsManagedRulesACFPRuleSet.responseInspection() != null) {
            ResponseInspectionResource responseInspectionResource = newSubresource(ResponseInspectionResource.class);
            responseInspectionResource.copyFrom(awsManagedRulesACFPRuleSet.responseInspection());
            setResponseInspection(responseInspectionResource);
        }
    }

    public AWSManagedRulesACFPRuleSet toAwsManagedRulesACFPRuleSet() {
        AWSManagedRulesACFPRuleSet.Builder builder = AWSManagedRulesACFPRuleSet.builder()
            .creationPath(getCreationPath())
            .registrationPagePath(getRegistrationPagePath());

        if (getEnableRegexInPath() != null) {
            builder.enableRegexInPath(getEnableRegexInPath());
        }

        if (getRequestInspection() != null) {
            builder.requestInspection(getRequestInspection().toRequestInspectionACFP());
        }

        if (getResponseInspection() != null) {
            builder.responseInspection(getResponseInspection().toResponseInspection());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getCreationPath() != null && getCreationPath().length() > 256) {
            errors.add(new ValidationError(this, null, "The param 'creation-path' must not exceed 256 characters in length."));
        }
        if (getRegistrationPagePath() != null && getRegistrationPagePath().length() > 256) {
            errors.add(new ValidationError(this, null, "The param 'registration-path' must not exceed 256 characters in length."));
        }

        return errors;
    }
}
