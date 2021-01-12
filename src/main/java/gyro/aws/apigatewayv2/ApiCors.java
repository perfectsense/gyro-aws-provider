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
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.apigatewayv2.model.Cors;

public class ApiCors extends Diffable implements Copyable<Cors> {

    private Boolean allowCredentials;
    private List<String> allowHeaders;
    private List<String> allowMethods;
    private List<String> allowOrigins;
    private List<String> exposeHeaders;
    private Integer maxAge;

    /**
     * When set to ``true`` credentials are included in the CORS request.
     */
    @Updatable
    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * The list of allowed headers.
     */
    @Updatable
    public List<String> getAllowHeaders() {
        if (allowHeaders == null) {
            allowHeaders = new ArrayList<>();
        }

        return allowHeaders;
    }

    public void setAllowHeaders(List<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    /**
     * The list of allowed HTTP methods.
     */
    @Updatable
    public List<String> getAllowMethods() {
        if (allowMethods == null) {
            allowMethods = new ArrayList<>();
        }

        return allowMethods;
    }

    public void setAllowMethods(List<String> allowMethods) {
        this.allowMethods = allowMethods;
    }

    /**
     * The list of allowed origins.
     */
    @Updatable
    public List<String> getAllowOrigins() {
        if (allowOrigins == null) {
            allowOrigins = new ArrayList<>();
        }

        return allowOrigins;
    }

    public void setAllowOrigins(List<String> allowOrigins) {
        this.allowOrigins = allowOrigins;
    }

    /**
     * The list of exposed headers.
     */
    @Updatable
    public List<String> getExposeHeaders() {
        if (exposeHeaders == null) {
            exposeHeaders = new ArrayList<>();
        }

        return exposeHeaders;
    }

    public void setExposeHeaders(List<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    /**
     * The number of seconds that the browser should cache preflight request results.
     */
    @Updatable
    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(Cors model) {
        setAllowCredentials(model.allowCredentials());
        setMaxAge(model.maxAge());

        getAllowHeaders().clear();
        if (model.hasAllowHeaders()) {
            setAllowHeaders(model.allowHeaders());
        }

        getAllowMethods().clear();
        if (model.hasAllowMethods()) {
            setAllowMethods(model.allowMethods());
        }

        getAllowOrigins().clear();
        if (model.hasAllowOrigins()) {
            setAllowOrigins(model.allowOrigins());
        }

        getExposeHeaders().clear();
        if (model.hasExposeHeaders()) {
            setExposeHeaders(model.exposeHeaders());
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getAllowCredentials() == null && getAllowHeaders() == null && getAllowMethods() == null
            && getAllowOrigins() == null && getExposeHeaders() == null && getMaxAge() == null) {
            errors.add(new ValidationError(this, null,
                "At least one of 'allow-credentials', 'allow-headers', 'allow-methods', 'allow-origins',"
                    + " 'expose-headers' or 'max-age' has to be set."));
        }

        return errors;
    }

    public Cors toCors() {
        return Cors.builder()
            .allowCredentials(getAllowCredentials())
            .allowHeaders(getAllowHeaders())
            .allowMethods(getAllowMethods())
            .allowOrigins(getAllowOrigins())
            .exposeHeaders(getExposeHeaders())
            .maxAge(getMaxAge())
            .build();
    }
}
