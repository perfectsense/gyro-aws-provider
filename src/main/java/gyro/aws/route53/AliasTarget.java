/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.route53;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.psddev.dari.util.StringUtils;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;

public class AliasTarget extends Diffable {

    private String dnsName;
    private String hostedZoneId;
    private HostedZoneResource hostedZone;
    private Boolean evaluateTargetHealth;

    /**
     * Dns name to associate with this Record Set.
     */
    @Required
    @Updatable
    public String getDnsName() {
        if (dnsName != null) {
            dnsName = StringUtils.ensureEnd(dnsName, ".");
        }

        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * The ID for the Hosted Zone where the 'dns name' belongs as configured.
     */
    @ConflictsWith("hosted-zone")
    @Updatable
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    /**
     * The Hosted Zone where the 'dns name' belongs as configured.
     */
    @ConflictsWith("hosted-zone-id")
    @Updatable
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    /**
     * Enable target health evaluation with this Record Set.
     */
    @Required
    @Updatable
    public Boolean getEvaluateTargetHealth() {
        return evaluateTargetHealth;
    }

    public void setEvaluateTargetHealth(Boolean evaluateTargetHealth) {
        this.evaluateTargetHealth = evaluateTargetHealth;
    }

    @Override
    public String primaryKey() {
        return "alias";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("hosted-zone") && !configuredFields.contains("hosted-zone-id")) {
            errors.add(new ValidationError(this, null, "Exactly one of 'hosted-zone' or 'hosted-zone-id' is required"));
        }

        return errors;
    }
}
