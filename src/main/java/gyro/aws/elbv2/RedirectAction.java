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

package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         type: "redirect"
 *
 *         redirect-action
 *             port: 443
 *             protocol: "HTTPS"
 *             status-code: "HTTP_301"
 *         end
 *     end
 */
public class RedirectAction extends Diffable implements Copyable<RedirectActionConfig> {
    private String host;
    private String path;
    private String port;
    private String protocol;
    private String query;
    private String statusCode;

    /**
     *  The hostname.
     */
    @Updatable
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     *  The absolute path starting with "/".
     */
    @Updatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *  The port.
     */
    @Updatable
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     *  The protocol.
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  The query parameters.
     */
    @Updatable
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     *  The HTTP redirect code.
     */
    @Updatable
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getPort(), getProtocol(), getStatusCode());
    }

    @Override
    public void copyFrom(RedirectActionConfig redirect) {
        setHost(redirect.host());
        setPath(redirect.path());
        setPort(redirect.port());
        setProtocol(redirect.protocol());
        setQuery(redirect.query());
        setStatusCode(redirect.statusCodeAsString());
    }

    public RedirectActionConfig toRedirect() {
        return RedirectActionConfig.builder()
                .host(getHost())
                .path(getPath())
                .port(getPort())
                .protocol(getProtocol())
                .query(getQuery())
                .statusCode(getStatusCode())
                .build();
    }
}
