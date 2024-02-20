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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatementCustomKey;

public class RateBasedStatementCustomKeyResource extends Diffable implements Copyable<RateBasedStatementCustomKey> {

    private RateLimitHeaderResource header;
    private RateLimitCookieResource cookie;
    private RateLimitQueryStringResource queryString;
    private RateLimitQueryArgumentResource queryArgument;
    private RateLimitHTTPMethodResource httpMethod;
    private RateLimitIPResource ip;
    private RateLimitLabelNamespaceResource labelNamespace;
    private RateLimitUriPathResource uriPath;
    private RateLimitForwardedIPResource forwardedIP;

    /**
     * The header to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitHeaderResource
     */
    public RateLimitHeaderResource getHeader() {
        return header;
    }

    public void setHeader(RateLimitHeaderResource header) {
        this.header = header;
    }

    /**
     * The cookie to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitCookieResource
     */
    public RateLimitCookieResource getCookie() {
        return cookie;
    }

    public void setCookie(RateLimitCookieResource cookie) {
        this.cookie = cookie;
    }

    /**
     * The query string to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitQueryStringResource
     */
    public RateLimitQueryStringResource getQueryString() {
        return queryString;
    }

    public void setQueryString(RateLimitQueryStringResource queryString) {
        this.queryString = queryString;
    }

    /**
     * The query argument to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitQueryArgumentResource
     */
    public RateLimitQueryArgumentResource getQueryArgument() {
        return queryArgument;
    }

    public void setQueryArgument(RateLimitQueryArgumentResource queryArgument) {
        this.queryArgument = queryArgument;
    }

    /**
     * The HTTP method to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitHTTPMethodResource
     */
    public RateLimitHTTPMethodResource getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(RateLimitHTTPMethodResource httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * The IP to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitIPResource
     */
    public RateLimitIPResource getIp() {
        return ip;
    }

    public void setIp(RateLimitIPResource ip) {
        this.ip = ip;
    }

    /**
     * The label namespace to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitLabelNamespaceResource
     */
    public RateLimitLabelNamespaceResource getLabelNamespace() {
        return labelNamespace;
    }

    public void setLabelNamespace(RateLimitLabelNamespaceResource labelNamespace) {
        this.labelNamespace = labelNamespace;
    }

    /**
     * The URI path to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitUriPathResource
     */
    public RateLimitUriPathResource getUriPath() {
        return uriPath;
    }

    public void setUriPath(RateLimitUriPathResource uriPath) {
        this.uriPath = uriPath;
    }

    /**
     * The forwarded IP to use for the rate limit.
     *
     * @subresource gyro.aws.wafv2.RateLimitForwardedIPResource
     */
    public RateLimitForwardedIPResource getForwardedIP() {
        return forwardedIP;
    }

    public void setForwardedIP(RateLimitForwardedIPResource forwardedIP) {
        this.forwardedIP = forwardedIP;
    }

    @Override
    public void copyFrom(RateBasedStatementCustomKey model) {

        setHeader(null);
        if (model.header() != null) {
            RateLimitHeaderResource header = newSubresource(RateLimitHeaderResource.class);
            header.copyFrom(model.header());
            setHeader(header);
        }

        setCookie(null);
        if (model.cookie() != null) {
            RateLimitCookieResource cookie = newSubresource(RateLimitCookieResource.class);
            cookie.copyFrom(model.cookie());
            setCookie(cookie);
        }

        setQueryString(null);
        if (model.queryString() != null) {
            RateLimitQueryStringResource queryString = newSubresource(RateLimitQueryStringResource.class);
            queryString.copyFrom(model.queryString());
            setQueryString(queryString);
        }

        setQueryArgument(null);
        if (model.queryArgument() != null) {
            RateLimitQueryArgumentResource queryArgument = newSubresource(RateLimitQueryArgumentResource.class);
            queryArgument.copyFrom(model.queryArgument());
            setQueryArgument(queryArgument);
        }

        setHttpMethod(null);
        if (model.httpMethod() != null) {
            RateLimitHTTPMethodResource httpMethod = newSubresource(RateLimitHTTPMethodResource.class);
            httpMethod.copyFrom(model.httpMethod());
            setHttpMethod(httpMethod);
        }

        setIp(null);
        if (model.ip() != null) {
            RateLimitIPResource ip = newSubresource(RateLimitIPResource.class);
            ip.copyFrom(model.ip());
            setIp(ip);
        }

        setLabelNamespace(null);
        if (model.labelNamespace() != null) {
            RateLimitLabelNamespaceResource labelNamespace = newSubresource(RateLimitLabelNamespaceResource.class);
            labelNamespace.copyFrom(model.labelNamespace());
            setLabelNamespace(labelNamespace);
        }

        setUriPath(null);
        if (model.uriPath() != null) {
            RateLimitUriPathResource uriPath = newSubresource(RateLimitUriPathResource.class);
            uriPath.copyFrom(model.uriPath());
            setUriPath(uriPath);
        }

        setForwardedIP(null);
        if (model.forwardedIP() != null) {
            RateLimitForwardedIPResource forwardedIP = newSubresource(RateLimitForwardedIPResource.class);
            forwardedIP.copyFrom(model.forwardedIP());
            setForwardedIP(forwardedIP);
        }
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        if (getHeader() != null) {
            sb.append("with header: ").append(getHeader().primaryKey());
        }

        if (getCookie() != null) {
            sb.append("with cookie: ").append(getCookie().primaryKey());
        }

        if (getQueryString() != null) {
            sb.append("with query string");
        }

        if (getQueryArgument() != null) {
            sb.append("with query argument: ").append(getQueryArgument().primaryKey());
        }

        if (getHttpMethod() != null) {
            sb.append("with http method");
        }

        if (getIp() != null) {
            sb.append("with ip");
        }

        if (getLabelNamespace() != null) {
            sb.append("with label namespace: ").append(getLabelNamespace().primaryKey());
        }

        if (getUriPath() != null) {
            sb.append("with uri path");
        }

        if (getForwardedIP() != null) {
            sb.append("with forwarded IP");
        }

        return sb.toString();
    }

    public RateBasedStatementCustomKey toRateBasedStatementCustomKey() {
        RateBasedStatementCustomKey.Builder builder = RateBasedStatementCustomKey.builder();

        if (getHeader() != null) {
            builder = builder.header(getHeader().toRateLimitHeader());
        }

        if (getCookie() != null) {
            builder = builder.cookie(getCookie().toRateLimitCookie());
        }

        if (getQueryString() != null) {
            builder = builder.queryString(getQueryString().toRateLimitQueryString());
        }

        if (getQueryArgument() != null) {
            builder = builder.queryArgument(getQueryArgument().toRateLimitQueryArgument());
        }

        if (getHttpMethod() != null) {
            builder = builder.httpMethod(getHttpMethod().toRateLimitHTTPMethod());
        }

        if (getIp() != null) {
            builder = builder.ip(getIp().toRateLimitIP());
        }

        if (getLabelNamespace() != null) {
            builder = builder.labelNamespace(getLabelNamespace().toRateLimitLabelNamespace());
        }

        if (getUriPath() != null) {
            builder = builder.uriPath(getUriPath().toRateLimitUriPath());
        }

        if (getForwardedIP() != null) {
            builder = builder.forwardedIP(getForwardedIP().toRateLimitForwardedIP());
        }

        return builder.build();
    }
}
