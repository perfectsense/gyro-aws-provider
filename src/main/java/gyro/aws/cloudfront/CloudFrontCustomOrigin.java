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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.CustomOriginConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CloudFrontCustomOrigin extends Diffable implements Copyable<CustomOriginConfig> {

    private Integer httpPort;
    private Integer httpsPort;
    private Integer originKeepAliveTimeout;
    private Integer originReadTimeout;
    private String originProtocolPolicy;
    private Set<String> originSslProtocols;

    /**
     * The port the origin listens for http.
     */
    @Updatable
    public Integer getHttpPort() {
        if (httpPort == null) {
            httpPort = 80;
        }

        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    /**
     * The port the origin listens for https.
     */
    @Updatable
    public Integer getHttpsPort() {
        if (httpsPort == null) {
            httpsPort = 443;
        }

        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    /**
     * The amount of time to keep an idle connection to the origin.
     */
    @Updatable
    public Integer getOriginKeepAliveTimeout() {
        if (originKeepAliveTimeout == null) {
            originKeepAliveTimeout = 5;
        }

        return originKeepAliveTimeout;
    }

    public void setOriginKeepAliveTimeout(Integer originKeepAliveTimeout) {
        this.originKeepAliveTimeout = originKeepAliveTimeout;
    }

    /**
     * The max amount of a time CloudFront will wait, in seconds, for an initial connection, and subsequent reads. Valid values are between 4 and 60.
     */
    @Updatable
    public Integer getOriginReadTimeout() {
        if (originReadTimeout == null) {
            originReadTimeout = 30;
        }

        return originReadTimeout;
    }

    public void setOriginReadTimeout(Integer originReadTimeout) {
        this.originReadTimeout = originReadTimeout;
    }

    /**
     * The protocol CloudFront should use to connect to the origin. Valid values are ``http-only``, ``https-only``, or ``match-viewer``.
     */
    @Updatable
    public String getOriginProtocolPolicy() {
        if (originProtocolPolicy == null) {
            originProtocolPolicy = "http-only";
        }

        return originProtocolPolicy;
    }

    public void setOriginProtocolPolicy(String originProtocolPolicy) {
        this.originProtocolPolicy = originProtocolPolicy;
    }

    /**
     * SSL protocols CloudFront is allow to connect to the origin with. Valid values are ``SSLv3``, ``TLSv1``, ``TLSv1.1``, ``TLSv1.2``.
     */
    @Updatable
    public Set<String> getOriginSslProtocols() {
        if (originSslProtocols == null) {
            originSslProtocols = new HashSet<>(Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2"));
        }

        return originSslProtocols;
    }

    public void setOriginSslProtocols(Set<String> originSslProtocols) {
        this.originSslProtocols = originSslProtocols;
    }

    @Override
    public void copyFrom(CustomOriginConfig originConfig) {
        setHttpPort(originConfig.httpPort());
        setHttpsPort(originConfig.httpsPort());
        setOriginKeepAliveTimeout(originConfig.originKeepaliveTimeout());
        setOriginProtocolPolicy(originConfig.originProtocolPolicyAsString());
        setOriginReadTimeout(originConfig.originReadTimeout());
        setOriginSslProtocols(new HashSet<>(originConfig.originSslProtocols().itemsAsStrings()));
    }

    @Override
    public String primaryKey() {
        return "custom-origin";
    }

    CustomOriginConfig toCustomOriginConfig() {
        return CustomOriginConfig.builder()
            .httpPort(getHttpPort())
            .httpsPort(getHttpsPort())
            .originKeepaliveTimeout(getOriginKeepAliveTimeout())
            .originProtocolPolicy(getOriginProtocolPolicy())
            .originReadTimeout(getOriginReadTimeout())
            .originSslProtocols(o -> o.itemsWithStrings(getOriginSslProtocols()).quantity(getOriginSslProtocols().size()))
            .build();
    }
}
