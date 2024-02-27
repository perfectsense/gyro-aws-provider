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

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.wafv2.model.AllQueryArguments;
import software.amazon.awssdk.services.wafv2.model.Body;
import software.amazon.awssdk.services.wafv2.model.Cookies;
import software.amazon.awssdk.services.wafv2.model.FieldToMatch;
import software.amazon.awssdk.services.wafv2.model.HeaderMatchPattern;
import software.amazon.awssdk.services.wafv2.model.HeaderOrder;
import software.amazon.awssdk.services.wafv2.model.Headers;
import software.amazon.awssdk.services.wafv2.model.JA3Fingerprint;
import software.amazon.awssdk.services.wafv2.model.JsonBody;
import software.amazon.awssdk.services.wafv2.model.Method;
import software.amazon.awssdk.services.wafv2.model.QueryString;
import software.amazon.awssdk.services.wafv2.model.SingleHeader;
import software.amazon.awssdk.services.wafv2.model.SingleQueryArgument;
import software.amazon.awssdk.services.wafv2.model.UriPath;

public class FieldToMatchResource extends Diffable implements Copyable<FieldToMatch> {

    private FieldMatchType matchType;
    private String name;
    private FieldMatchBodyResource body;
    private FieldMatchHeadersResource headers;
    private FieldMatchCookiesResource cookies;
    private FieldMatchHeaderOrderResource headerOrder;
    private FieldMatchJsonBodyResource jsonBody;
    private FieldMatchJa3FingerprintResource ja3Fingerprint;

    /**
     * The field match type.
     */
    @Required
    @ValidStrings({"SINGLE_HEADER", "SINGLE_QUERY_ARGUMENT", "ALL_QUERY_ARGUMENTS", "BODY", "QUERY_STRING", "METHOD", "URI_PATH", "HEADERS", "COOKIES", "HEADER_ORDER", "JSON_BODY", "JA3_FINGERPRINT"})
    public FieldMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(FieldMatchType matchType) {
        this.matchType = matchType;
    }

    /**
     * The name of the field to match. Only required if ``match-type`` set to ``SINGLE_HEADER`` or ``SINGLE_QUERY_ARGUMENT``.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The body to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchBodyResource
     */
    public FieldMatchBodyResource getBody() {
        return body;
    }

    public void setBody(FieldMatchBodyResource body) {
        this.body = body;
    }

    /**
     * The headers to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchHeadersResource
     */
    public FieldMatchHeadersResource getHeaders() {
        return headers;
    }

    public void setHeaders(FieldMatchHeadersResource headers) {
        this.headers = headers;
    }

    /**
     * The cookies to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchCookiesResource
     */
    public FieldMatchCookiesResource getCookies() {
        return cookies;
    }

    public void setCookies(FieldMatchCookiesResource cookies) {
        this.cookies = cookies;
    }

    /**
     * The header order to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchHeaderOrderResource
     */
    public FieldMatchHeaderOrderResource getHeaderOrder() {
        return headerOrder;
    }

    public void setHeaderOrder(FieldMatchHeaderOrderResource headerOrder) {
        this.headerOrder = headerOrder;
    }

    /**
     * The JSON body to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchJsonBodyResource
     */
    public FieldMatchJsonBodyResource getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(FieldMatchJsonBodyResource jsonBody) {
        this.jsonBody = jsonBody;
    }

    /**
     * The JA3 fingerprint to match.
     *
     * @subresource gyro.aws.wafv2.FieldMatchJa3FingerprintResource
     */
    public FieldMatchJa3FingerprintResource getJa3Fingerprint() {
        return ja3Fingerprint;
    }

    public void setJa3Fingerprint(FieldMatchJa3FingerprintResource ja3Fingerprint) {
        this.ja3Fingerprint = ja3Fingerprint;
    }

    @Override
    public String primaryKey() {
        return String.format(
            "match type: '%s'%s",
            getMatchType(),
            (ObjectUtils.isBlank(getName()) ? "" : String.format(", field: '%s'", getName())));
    }

    @Override
    public void copyFrom(FieldToMatch fieldToMatch) {
        setMatchType(null);
        setName(null);
        setBody(null);
        setHeaders(null);
        setCookies(null);
        setHeaderOrder(null);
        setJsonBody(null);
        setJa3Fingerprint(null);

        if (fieldToMatch.body() != null) {
            matchType = FieldMatchType.BODY;
            FieldMatchBodyResource body = newSubresource(FieldMatchBodyResource.class);
            body.copyFrom(fieldToMatch.body());
            setBody(body);
        } else if (fieldToMatch.allQueryArguments() != null) {
            matchType = FieldMatchType.ALL_QUERY_ARGUMENTS;
        } else if (fieldToMatch.queryString() != null) {
            matchType = FieldMatchType.QUERY_STRING;
        } else if (fieldToMatch.method() != null) {
            matchType = FieldMatchType.METHOD;
        } else if (fieldToMatch.uriPath() != null) {
            matchType = FieldMatchType.URI_PATH;
        } else if (fieldToMatch.singleHeader() != null) {
            matchType = FieldMatchType.SINGLE_HEADER;
            setName(fieldToMatch.singleHeader().name());
        } else if (fieldToMatch.singleQueryArgument() != null) {
            matchType = FieldMatchType.SINGLE_QUERY_ARGUMENT;
            setName(fieldToMatch.singleQueryArgument().name());
        } else if (fieldToMatch.headers() != null) {
            matchType = FieldMatchType.HEADERS;
            FieldMatchHeadersResource headers = newSubresource(FieldMatchHeadersResource.class);
            headers.copyFrom(fieldToMatch.headers());
            setHeaders(headers);
        } else if (fieldToMatch.cookies() != null) {
            matchType = FieldMatchType.COOKIES;
            FieldMatchCookiesResource cookies = newSubresource(FieldMatchCookiesResource.class);
            cookies.copyFrom(fieldToMatch.cookies());
            setCookies(cookies);
        } else if (fieldToMatch.headerOrder() != null) {
            matchType = FieldMatchType.HEADER_ORDER;
            FieldMatchHeaderOrderResource headerOrder = newSubresource(FieldMatchHeaderOrderResource.class);
            headerOrder.copyFrom(fieldToMatch.headerOrder());
            setHeaderOrder(headerOrder);
        } else if (fieldToMatch.jsonBody() != null) {
            matchType = FieldMatchType.JSON_BODY;
            FieldMatchJsonBodyResource jsonBody = newSubresource(FieldMatchJsonBodyResource.class);
            jsonBody.copyFrom(fieldToMatch.jsonBody());
            setJsonBody(jsonBody);
        } else if (fieldToMatch.ja3Fingerprint() != null) {
            matchType = FieldMatchType.JA3_FINGERPRINT;
            FieldMatchJa3FingerprintResource ja3Fingerprint = newSubresource(FieldMatchJa3FingerprintResource.class);
            ja3Fingerprint.copyFrom(fieldToMatch.ja3Fingerprint());
            setJa3Fingerprint(ja3Fingerprint);
        }

        setMatchType(matchType);
    }

    FieldToMatch toFieldToMatch() {
        FieldToMatch.Builder builder = FieldToMatch.builder();

        if (getMatchType() == FieldMatchType.BODY) {
            builder.body(getBody().toBody());
        } else if (getMatchType() == FieldMatchType.ALL_QUERY_ARGUMENTS) {
            builder.allQueryArguments(AllQueryArguments.builder().build());
        } else if (getMatchType() == FieldMatchType.QUERY_STRING) {
            builder.queryString(QueryString.builder().build());
        } else if (getMatchType() == FieldMatchType.METHOD) {
            builder.method(Method.builder().build());
        } else if (getMatchType() == FieldMatchType.URI_PATH) {
            builder.uriPath(UriPath.builder().build());
        } else if (getMatchType() == FieldMatchType.SINGLE_HEADER) {
            builder.singleHeader(SingleHeader.builder()
                .name(getName())
                .build());
        } else if (getMatchType() == FieldMatchType.SINGLE_QUERY_ARGUMENT) {
            builder.singleQueryArgument(SingleQueryArgument.builder()
                .name(getName())
                .build());
        } else if (getMatchType() == FieldMatchType.HEADERS) {
            builder.headers(getHeaders().toHeaders());
        } else if (getMatchType() == FieldMatchType.COOKIES) {
            builder.cookies(getCookies().toCookies());
        } else if (getMatchType() == FieldMatchType.HEADER_ORDER) {
            builder.headerOrder(getHeaderOrder().toHeaderOrder());
        } else if (getMatchType() == FieldMatchType.JSON_BODY) {
            builder.jsonBody(getJsonBody().toJsonBody());
        } else if (getMatchType() == FieldMatchType.JA3_FINGERPRINT) {
            builder.ja3Fingerprint(getJa3Fingerprint().toJa3Fingerprint());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getName() != null && getMatchType() != FieldMatchType.SINGLE_QUERY_ARGUMENT
            && getMatchType() != FieldMatchType.SINGLE_HEADER) {
            errors.add(new ValidationError(
                this,
                "name",
                "'name' cannot be set if 'match-type' is not set to either 'SINGLE_HEADER' or 'SINGLE_QUERY_ARGUMENT'"));
        } else if (getName() == null && (getMatchType() == FieldMatchType.SINGLE_QUERY_ARGUMENT
            || getMatchType() == FieldMatchType.SINGLE_HEADER)) {
            errors.add(new ValidationError(
                this,
                "name",
                "'name' is required if 'match-type' is set to either 'SINGLE_HEADER' or 'SINGLE_QUERY_ARGUMENT'"));
        }

        if (getMatchType() == FieldMatchType.BODY && getBody() == null) {
            errors.add(new ValidationError(
                this,
                "body",
                "'body' is required if 'match-type' is set to 'BODY'"));
        } else if (getMatchType() == FieldMatchType.HEADERS && getHeaders() == null) {
            errors.add(new ValidationError(
                this,
                "headers",
                "'headers' is required if 'match-type' is set to 'HEADERS'"));
        } else if (getMatchType() == FieldMatchType.COOKIES && getCookies() == null) {
            errors.add(new ValidationError(
                this,
                "cookies",
                "'cookies' is required if 'match-type' is set to 'COOKIES'"));
        } else if (getMatchType() == FieldMatchType.HEADER_ORDER && getHeaderOrder() == null) {
            errors.add(new ValidationError(
                this,
                "header-order",
                "'header-order' is required if 'match-type' is set to 'HEADER_ORDER'"));
        } else if (getMatchType() == FieldMatchType.JSON_BODY && getJsonBody() == null) {
            errors.add(new ValidationError(
                this,
                "json-body",
                "'json-body' is required if 'match-type' is set to 'JSON_BODY'"));
        } else if (getMatchType() == FieldMatchType.JA3_FINGERPRINT && getJa3Fingerprint() == null) {
            errors.add(new ValidationError(
                this,
                "ja3-fingerprint",
                "'ja3-fingerprint' is required if 'match-type' is set to 'JA3_FINGERPRINT'"));
        }

        if (getMatchType() != FieldMatchType.BODY && getBody() != null) {
            errors.add(new ValidationError(
                this,
                "body",
                "'body' can only be set if 'match-type' is set to 'BODY'"));
        } else if (getMatchType() != FieldMatchType.HEADERS && getHeaders() != null) {
            errors.add(new ValidationError(
                this,
                "headers",
                "'headers' can only be set if 'match-type' is set to 'HEADERS'"));
        } else if (getMatchType() != FieldMatchType.COOKIES && getCookies() != null) {
            errors.add(new ValidationError(
                this,
                "cookies",
                "'cookies' can only be set if 'match-type' is set to 'COOKIES'"));
        } else if (getMatchType() != FieldMatchType.HEADER_ORDER && getHeaderOrder() != null) {
            errors.add(new ValidationError(
                this,
                "header-order",
                "'header-order' can only be set if 'match-type' is set to 'HEADER_ORDER'"));
        } else if (getMatchType() != FieldMatchType.JSON_BODY && getJsonBody() != null) {
            errors.add(new ValidationError(
                this,
                "json-body",
                "'json-body' can only be set if 'match-type' is set to 'JSON_BODY'"));
        } else if (getMatchType() != FieldMatchType.JA3_FINGERPRINT && getJa3Fingerprint() != null) {
            errors.add(new ValidationError(
                this,
                "ja3-fingerprint",
                "'ja3-fingerprint' can only be set if 'match-type' is set to 'JA3_FINGERPRINT'"));
        }

        return errors;
    }
}
