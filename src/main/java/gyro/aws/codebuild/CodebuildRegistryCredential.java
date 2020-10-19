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

package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.model.CredentialProviderType;
import software.amazon.awssdk.services.codebuild.model.RegistryCredential;

public class CodebuildRegistryCredential extends Diffable implements Copyable<RegistryCredential> {

    private String credential;
    private CredentialProviderType credentialProvider;

    /**
     * The Amazon Resource Name (ARN) or name of credentials created using AWS Secrets Manager.
     */
    @Required
    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    /**
     * The service that created the credentials to access a private Docker registry. Valid values are
     * ``SECRETS_MANAGER``. (Required)
     */
    @Required
    public CredentialProviderType getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(CredentialProviderType credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    @Override
    public void copyFrom(RegistryCredential model) {
        setCredential(model.credential());
        setCredentialProvider(model.credentialProvider());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public RegistryCredential toRegistryCredential() {
        return RegistryCredential.builder()
            .credential(getCredential())
            .credentialProvider(getCredentialProvider())
            .build();
    }
}
