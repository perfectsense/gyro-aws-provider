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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.model.JWTConfiguration;

public class ApiJwtConfiguration extends Diffable implements Copyable<JWTConfiguration> {

    private List<String> audiences;
    private String issuer;

    /**
     * The list of the intended recipients of the JWT.
     */
    @Updatable
    @Required
    public List<String> getAudiences() {
        if (audiences == null) {
            audiences = new ArrayList<>();
        }

        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    /**
     * The base domain of the identity provider that issues JSON Web Tokens.
     */
    @Updatable
    @Required
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(JWTConfiguration model) {
        setAudiences(model.hasAudience() ? model.audience() : null);
        setIssuer(model.issuer());
    }

    public JWTConfiguration toJWTConfiguration() {
        return JWTConfiguration.builder().audience(getAudiences()).issuer(getIssuer()).build();
    }
}
