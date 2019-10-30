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

package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.LayerVersionsListItem;
import software.amazon.awssdk.services.lambda.model.LayersListItem;
import software.amazon.awssdk.services.lambda.model.ListLayerVersionsRequest;
import software.amazon.awssdk.services.lambda.model.ListLayerVersionsResponse;
import software.amazon.awssdk.services.lambda.model.ListLayersRequest;
import software.amazon.awssdk.services.lambda.model.ListLayersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query lambda layer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lambda-layer: $(external-query aws::lambda-layer { name: '' and version = ''})
 */
@Type("lambda-layer")
public class LayerFinder extends AwsFinder<LambdaClient, GetLayerVersionResponse, LayerResource> {
    private String name;
    private String version;

    /**
     * The layer name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The layer version number.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    protected List<GetLayerVersionResponse> findAllAws(LambdaClient client) {
        List<GetLayerVersionResponse> getLayerVersions = new ArrayList<>();

        String marker = null;
        List<LayersListItem> layersListItems = new ArrayList<>();
        ListLayersResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listLayers();
            } else {
                response = client.listLayers(ListLayersRequest.builder().marker(marker).build());
            }

            marker = response.nextMarker();
            layersListItems.addAll(response.layers());

        } while (!ObjectUtils.isBlank(marker));

        layersListItems.forEach(o -> getLayerVersions.addAll(getAllLayerVersions(client, o.layerName())));
        return getLayerVersions;
    }

    @Override
    protected List<GetLayerVersionResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetLayerVersionResponse> getLayerVersions = new ArrayList<>();

        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        if (filters.containsKey("version") && !isValidLong(filters.get("version"))) {
            throw new IllegalArgumentException("'version' needs to be valid long.");
        }

        if (filters.containsKey("version")) {
            getLayerVersions.add(
                client.getLayerVersion(
                    r -> r.layerName(filters.get("name"))
                        .versionNumber(Long.parseLong(filters.get("version")))));
        } else {
            getLayerVersions.addAll(getAllLayerVersions(client, filters.get("name")));
        }

        return getLayerVersions;
    }

    private List<GetLayerVersionResponse> getAllLayerVersions(LambdaClient client, String layerName) {
        List<GetLayerVersionResponse> getLayerVersions = new ArrayList<>();
        List<Long> versions = new ArrayList<>();
        ListLayerVersionsResponse response;
        String marker = "";

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listLayerVersions(ListLayerVersionsRequest.builder().layerName(layerName).build());
            } else {
                response = client.listLayerVersions(ListLayerVersionsRequest.builder().layerName(layerName).marker(marker).build());
            }

            marker = response.nextMarker();
            versions.addAll(response.layerVersions().stream().map(LayerVersionsListItem::version).collect(Collectors.toList()));
        } while (!ObjectUtils.isBlank(marker));

        for (Long version : versions) {
            getLayerVersions.add(client.getLayerVersion(r -> r.layerName(layerName).versionNumber(version)));
        }

        return getLayerVersions;
    }

    private boolean isValidLong(String val) {
        if (val == null) {
            return false;
        }

        try {
            Long.parseLong(val);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }
}
