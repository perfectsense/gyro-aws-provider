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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.model.DomainEndpointOptions;
import software.amazon.awssdk.services.opensearch.model.TLSSecurityPolicy;

public class OpenSearchDomainEndpointOptions extends Diffable implements Copyable<DomainEndpointOptions> {

    private Boolean enforceHttps;
    private TLSSecurityPolicy tlsSecurityPolicy;

    /**
     * When set to ``true``, all traffic to the domain will be required to arrive over HTTPS
     */
    @Required
    @Updatable
    public Boolean getEnforceHttps() {
        return enforceHttps;
    }

    public void setEnforceHttps(Boolean enforceHttps) {
        this.enforceHttps = enforceHttps;
    }

    /**
     * The TLS security policy that needs to be applied to the HTTPS endpoints of the OpenSearch domain.
     */
    @Updatable
    @ValidStrings({ "Policy-Min-TLS-1-0-2019-07", "Policy-Min-TLS-1-2-2019-07", "Policy-Min-TLS-1-2-PFS-2023-10" })
    public TLSSecurityPolicy getTlsSecurityPolicy() {
        return tlsSecurityPolicy;
    }

    public void setTlsSecurityPolicy(TLSSecurityPolicy tlsSecurityPolicy) {
        this.tlsSecurityPolicy = tlsSecurityPolicy;
    }

    @Override
    public void copyFrom(DomainEndpointOptions model) {
        setEnforceHttps(model.enforceHTTPS());
        setTlsSecurityPolicy(model.tlsSecurityPolicy());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    DomainEndpointOptions toDomainEndpointOptions() {
        return DomainEndpointOptions.builder()
            .enforceHTTPS(getEnforceHttps())
            .tlsSecurityPolicy(getTlsSecurityPolicy())
            .build();
    }
}
