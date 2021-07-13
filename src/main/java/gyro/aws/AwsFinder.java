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

package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.GyroException;
import gyro.core.finder.Finder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsFinder<C extends SdkClient, M, R extends AwsResource> extends Finder<R> {

    protected abstract List<M> findAllAws(C client);

    @Override
    public final List<R> findAll() {
        return findAllAws(newClient()).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    protected C newClient() {
        @SuppressWarnings("unchecked")
        Class<C> clientClass = (Class<C>) TypeDefinition.getInstance(getClass())
            .getInferredGenericTypeArgumentClass(AwsFinder.class, 0);

        return AwsResource.createClient(clientClass, credentials(AwsCredentials.class), getRegion(), getEndpoint(), null);
    }

    protected String getRegion() {
        return null;
    }

    protected String getEndpoint() {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected R newResource(M model) {
        R resource = newResource();

        if (resource instanceof Copyable) {
            ((Copyable<M>) resource).copyFrom(model);
        }

        return resource;
    }

    protected abstract List<M> findAws(C client, Map<String, String> filters);

    @Override
    public List<R> find(Map<String, Object> filters) {
        return findAws(newClient(), convertFilters(filters)).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    public List<Filter> createFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    public List<software.amazon.awssdk.services.rds.model.Filter> createRdsFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> software.amazon.awssdk.services.rds.model.Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    public List<software.amazon.awssdk.services.neptune.model.Filter> createNeptuneFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> software.amazon.awssdk.services.neptune.model.Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertFilters(Map<String, Object> query) {
        Map<String, String> filters = new HashMap<>();

        for (Map.Entry<String, Object> e : query.entrySet()) {
            filters.put(e.getKey(), e.getValue().toString());
        }

        return filters;
    }

}
