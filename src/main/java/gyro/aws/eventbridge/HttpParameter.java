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

package gyro.aws.eventbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eventbridge.model.HttpParameters;

public class HttpParameter extends Diffable implements Copyable<HttpParameters> {

    private Map<String, String> headerParameters;
    private Map<String, String> queryStringParameters;
    private List<String> pathParameterValues;

    public Map<String, String> getHeaderParameters() {
        if (headerParameters == null) {
            headerParameters = new HashMap<>();
        }

        return headerParameters;
    }

    public void setHeaderParameters(Map<String, String> headerParameters) {
        this.headerParameters = headerParameters;
    }

    public Map<String, String> getQueryStringParameters() {
        if (queryStringParameters == null) {
            queryStringParameters = new HashMap<>();
        }

        return queryStringParameters;
    }

    public void setQueryStringParameters(Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    public List<String> getPathParameterValues() {
        if (pathParameterValues == null) {
            pathParameterValues = new ArrayList<>();
        }

        return pathParameterValues;
    }

    public void setPathParameterValues(List<String> pathParameterValues) {
        this.pathParameterValues = pathParameterValues;
    }

    @Override
    public void copyFrom(HttpParameters model) {
        setHeaderParameters(model.headerParameters());
        setPathParameterValues(model.pathParameterValues());
        setQueryStringParameters(model.queryStringParameters());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    protected HttpParameters toHttpParameters() {
        return HttpParameters.builder()
            .headerParameters(getHeaderParameters())
            .queryStringParameters(getQueryStringParameters())
            .pathParameterValues(getPathParameterValues())
            .build();
    }
}
