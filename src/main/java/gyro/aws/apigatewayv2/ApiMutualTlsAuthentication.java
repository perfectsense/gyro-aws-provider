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

package gyro.aws.apigatewayv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.model.MutualTlsAuthentication;
import software.amazon.awssdk.services.apigatewayv2.model.MutualTlsAuthenticationInput;

public class ApiMutualTlsAuthentication extends Diffable implements Copyable<MutualTlsAuthentication> {

    private String truststoreUri;
    private String truststoreVersion;

    /**
     * An Amazon S3 URL that specifies the truststore for mutual TLS authentication.
     */
    @Required
    public String getTruststoreUri() {
        return truststoreUri;
    }

    public void setTruststoreUri(String truststoreUri) {
        this.truststoreUri = truststoreUri;
    }

    /**
     * The version of the S3 object that contains your truststore.
     */
    public String getTruststoreVersion() {
        return truststoreVersion;
    }

    public void setTruststoreVersion(String truststoreVersion) {
        this.truststoreVersion = truststoreVersion;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(MutualTlsAuthentication model) {
        setTruststoreUri(model.truststoreUri());
        setTruststoreVersion(model.truststoreVersion());
    }

    public MutualTlsAuthenticationInput toMutualTlsAuthenticationInput() {
        return MutualTlsAuthenticationInput.builder()
            .truststoreUri(getTruststoreUri())
            .truststoreVersion(getTruststoreVersion())
            .build();
    }
}
