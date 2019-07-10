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
 * .. code-block:: gyro
 *
 *    lambda-layer: $(aws::lambda-layer EXTERNAL/* | layer-name = '' and version = '')
 */
@Type("lambda-layer")
public class LayerFinder extends AwsFinder<LambdaClient, GetLayerVersionResponse, LayerResource> {
    private String layerName;
    private String version;

    /**
     * The layer name.
     */
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
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

        if (!filters.containsKey("layer-name")) {
            throw new IllegalArgumentException("'layer-name' is required.");
        }

        if (filters.containsKey("version") && !isValidLong(filters.get("version"))) {
            throw new IllegalArgumentException("'version' needs to be valid long.");
        }

        if (filters.containsKey("version")) {
            getLayerVersions.add(
                client.getLayerVersion(
                    r -> r.layerName(filters.get("layer-name"))
                        .versionNumber(Long.parseLong(filters.get("version")))));
        } else {
            getLayerVersions.addAll(getAllLayerVersions(client, filters.get("layer-name")));
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
