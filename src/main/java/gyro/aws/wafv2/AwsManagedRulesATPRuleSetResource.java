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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.AWSManagedRulesATPRuleSet;

public class AwsManagedRulesATPRuleSetResource extends Diffable implements Copyable<AWSManagedRulesATPRuleSet> {

    private String loginPath;
    private Boolean enableRegexInPath;
    private RequestInspectionResource requestInspection;
    private ResponseInspectionResource responseInspection;

    /**
     * Path of the login endpoint.
     */
    @Required
    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    /**
     * Whether regular expressions are allowed in the login path.
     */
    public Boolean getEnableRegexInPath() {
        return enableRegexInPath;
    }

    public void setEnableRegexInPath(Boolean enableRegexInPath) {
        this.enableRegexInPath = enableRegexInPath;
    }

    /**
     * Criteria for inspecting login requests.
     *
     * @subresource gyro.aws.wafv2.RequestInspectionResource
     */
    public RequestInspectionResource getRequestInspection() {
        return requestInspection;
    }

    public void setRequestInspection(RequestInspectionResource requestInspection) {
        this.requestInspection = requestInspection;
    }

    /**
     * Criteria for inspecting responses to login requests.
     *
     * @subresource gyro.aws.wafv2.ResponseInspectionResource
     */
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
    public void copyFrom(AWSManagedRulesATPRuleSet awsManagedRulesATPRuleSet) {
        setLoginPath(awsManagedRulesATPRuleSet.loginPath());
        setEnableRegexInPath(awsManagedRulesATPRuleSet.enableRegexInPath());

        setRequestInspection(null);
        if (awsManagedRulesATPRuleSet.requestInspection() != null) {
            RequestInspectionResource resource = newSubresource(RequestInspectionResource.class);
            resource.copyFrom(awsManagedRulesATPRuleSet.requestInspection());
            setRequestInspection(resource);
        }

        setResponseInspection(null);
        if (awsManagedRulesATPRuleSet.responseInspection() != null) {
            ResponseInspectionResource resource = newSubresource(ResponseInspectionResource.class);
            resource.copyFrom(awsManagedRulesATPRuleSet.responseInspection());
            setResponseInspection(resource);
        }
    }

    AWSManagedRulesATPRuleSet toAwsManagedRulesATPRuleSet() {
        AWSManagedRulesATPRuleSet.Builder builder = AWSManagedRulesATPRuleSet.builder()
            .loginPath(getLoginPath());

        if (getEnableRegexInPath() != null) {
            builder.enableRegexInPath(getEnableRegexInPath());
        }
        if (getRequestInspection() != null) {
            builder.requestInspection(getRequestInspection().toRequestInspection());
        }
        if (getResponseInspection() != null) {
            builder.responseInspection(getResponseInspection().toResponseInspection());
        }

        return builder.build();
    }
}
