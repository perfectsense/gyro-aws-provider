/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearchserverless;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearchserverless.model.CreateIamIdentityCenterConfigOptions;
import software.amazon.awssdk.services.opensearchserverless.model.IamIdentityCenterConfigOptions;
import software.amazon.awssdk.services.opensearchserverless.model.IamIdentityCenterGroupAttribute;
import software.amazon.awssdk.services.opensearchserverless.model.IamIdentityCenterUserAttribute;
import software.amazon.awssdk.services.opensearchserverless.model.UpdateIamIdentityCenterConfigOptions;

public class OpenSearchServerlessIamIdentityCenterConfig extends Diffable
    implements Copyable<IamIdentityCenterConfigOptions> {

    private IamIdentityCenterUserAttribute userAttribute;
    private IamIdentityCenterGroupAttribute groupAttribute;
    private String applicationArn;
    private String applicationDescription;
    private String applicationName;
    private String instanceArn;

    /**
     * The user attribute to use for the IAM identity center.
     */
    @Updatable
    @ValidStrings({ "UserId", "Email", "UserName" })
    public IamIdentityCenterUserAttribute getUserAttribute() {
        return userAttribute;
    }

    public void setUserAttribute(IamIdentityCenterUserAttribute userAttribute) {
        this.userAttribute = userAttribute;
    }

    /**
     * The group attribute to use for the IAM identity center.
     */
    @Updatable
    @ValidStrings({ "GroupName", "GroupId" })
    public IamIdentityCenterGroupAttribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(IamIdentityCenterGroupAttribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    /**
     * The ARN of the instance.
     */
    @Required
    public String getInstanceArn() {
        return instanceArn;
    }

    public void setInstanceArn(String instanceArn) {
        this.instanceArn = instanceArn;
    }

    /**
     * The ARN of the application.
     */
    @Output
    public String getApplicationArn() {
        return applicationArn;
    }

    public void setApplicationArn(String applicationArn) {
        this.applicationArn = applicationArn;
    }

    /**
     * The description of the application.
     */
    @Output
    public String getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    /**
     * The name of the application.
     */
    @Output
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void copyFrom(IamIdentityCenterConfigOptions model) {
        setUserAttribute(model.userAttribute());
        setGroupAttribute(model.groupAttribute());
        setInstanceArn(model.instanceArn());
        setApplicationArn(model.applicationArn());
        setApplicationDescription(model.applicationDescription());
        setApplicationName(model.applicationName());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CreateIamIdentityCenterConfigOptions toIamIdentityCenterConfigOptionsCreate() {
        return CreateIamIdentityCenterConfigOptions.builder()
            .userAttribute(getUserAttribute())
            .groupAttribute(getGroupAttribute())
            .instanceArn(getInstanceArn())
            .build();
    }

    UpdateIamIdentityCenterConfigOptions toIamIdentityCenterConfigOptionsUpdate() {
        return UpdateIamIdentityCenterConfigOptions.builder()
            .userAttribute(getUserAttribute())
            .groupAttribute(getGroupAttribute())
            .build();
    }
}
