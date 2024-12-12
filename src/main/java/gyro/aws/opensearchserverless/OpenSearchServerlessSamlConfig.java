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

import java.io.IOException;
import java.io.InputStream;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearchserverless.model.SamlConfigOptions;
import software.amazon.awssdk.utils.IoUtils;

public class OpenSearchServerlessSamlConfig extends Diffable implements Copyable<SamlConfigOptions> {

    private String groupAttribute;
    private String metadata;
    private Integer sessionTimeout;
    private String userAttribute;

    /**
     * The group attribute to use for the SAML configuration.
     */
    @Required
    @Updatable
    public String getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(String groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    /**
     * The metadata document to use for the SAML configuration. A xml path or xml string is allowed.
     */
    @Updatable
    public String getMetadata() {
        if (metadata != null && metadata.contains(".xml")) {
            try (InputStream input = openInput(metadata)) {
                metadata = IoUtils.toUtf8String(input);
                return metadata;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return metadata;
        }
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * The session timeout in minutes for the SAML configuration.
     */
    @Required
    @Updatable
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * The user attribute to use for the SAML configuration.
     */
    @Updatable
    public String getUserAttribute() {
        return userAttribute;
    }

    public void setUserAttribute(String userAttribute) {
        this.userAttribute = userAttribute;
    }

    @Override
    public void copyFrom(SamlConfigOptions model) {
        setGroupAttribute(model.groupAttribute());
        setMetadata(model.metadata());
        setSessionTimeout(model.sessionTimeout());
        setUserAttribute(model.userAttribute());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SamlConfigOptions toSamlConfigOptions() {
        return SamlConfigOptions.builder()
            .groupAttribute(getGroupAttribute())
            .metadata(getMetadata())
            .sessionTimeout(getSessionTimeout())
            .userAttribute(getUserAttribute())
            .build();
    }
}
