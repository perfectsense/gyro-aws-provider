/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControlOriginTypes;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControlSigningBehaviors;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControlSigningProtocols;

public class OriginAccessControlConfig extends Diffable
    implements Copyable<software.amazon.awssdk.services.cloudfront.model.OriginAccessControlConfig> {

    private String name;
    private String description;
    private OriginAccessControlOriginTypes originType;
    private OriginAccessControlSigningBehaviors signingBehavior;
    private OriginAccessControlSigningProtocols signingProtocol;

    /**
     * The name of the Origin Access Control (OAC).
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the Origin Access Control (OAC).
     */
    @Updatable
    @Required
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The type of origin this Origin Access Control (OAC) is valid for.
     */
    @ValidStrings("S3")
    @Updatable
    @Required
    public OriginAccessControlOriginTypes getOriginType() {
        return originType;
    }

    public void setOriginType(OriginAccessControlOriginTypes originType) {
        this.originType = originType;
    }

    /**
     * The behaviour of the Origin Access Control (OAC) that determine which requests Cloudfront signs.
     */
    @ValidStrings({"NEVER", "ALWAYS", "NO_OVERRIDE"})
    @Updatable
    @Required
    public OriginAccessControlSigningBehaviors getSigningBehavior() {
        return signingBehavior;
    }

    public void setSigningBehavior(OriginAccessControlSigningBehaviors signingBehavior) {
        this.signingBehavior = signingBehavior;
    }

    /**
     * The signing protocol of the Origin Access Control (OAC) that determines how the Cloudfront signs requests.
     */
    @Required
    public OriginAccessControlSigningProtocols getSigningProtocol() {
        return signingProtocol;
    }

    public void setSigningProtocol(OriginAccessControlSigningProtocols signingProtocol) {
        this.signingProtocol = signingProtocol;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.OriginAccessControlConfig model) {
        setName(model.name());
        setDescription(model.description());
        setOriginType(model.originAccessControlOriginType());
        setSigningBehavior(model.signingBehavior());
        setSigningProtocol(model.signingProtocol());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public software.amazon.awssdk.services.cloudfront.model.OriginAccessControlConfig toOriginAccessControlConfig() {
        return software.amazon.awssdk.services.cloudfront.model.OriginAccessControlConfig.builder()
            .name(getName())
            .description(getDescription())
            .originAccessControlOriginType(getOriginType())
            .signingBehavior(getSigningBehavior())
            .signingProtocol(getSigningProtocol())
            .build();
    }
}
