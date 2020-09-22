/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.elasticsearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticsearch.model.DomainEndpointOptions;
import software.amazon.awssdk.services.elasticsearch.model.TLSSecurityPolicy;

public class ElasticsearchDomainEndpointOptions extends Diffable implements Copyable<DomainEndpointOptions> {

    private Boolean enforceHttps;
    private TLSSecurityPolicy tlsSecurityPolicy;

    /**
     * Enforce HTTPS endpoints for the Elasticsearch cluster. (Required)
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
     * The TLS security policy that needs to be applied to the HTTPS endpoints of the Elasticsearch domain.
     */
    @Updatable
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
