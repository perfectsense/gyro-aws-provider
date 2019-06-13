package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.LayerVersionsListItem;
import software.amazon.awssdk.services.lambda.model.ListLayerVersionsResponse;

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
        client.listLayers().layers().forEach(o -> getLayerVersions.addAll(getAllLayerVersions(client, o.layerName())));
        return getLayerVersions;
    }

    @Override
    protected List<GetLayerVersionResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetLayerVersionResponse> getLayerVersions = new ArrayList<>();

        if (filters.containsKey("layer-name") && !ObjectUtils.isBlank(filters.get("layer-name"))) {
            if (filters.containsKey("version") && isValidLong(filters.get("version"))) {
                getLayerVersions.add(
                    client.getLayerVersion(
                        r -> r.layerName(filters.get("layer-name"))
                            .versionNumber(Long.parseLong(filters.get("version")))
                    )
                );
            }

            if (!filters.containsKey("version")) {
                getLayerVersions.addAll(getAllLayerVersions(client, filters.get("layer-name")));
            }
        }

        return getLayerVersions;
    }

    private List<GetLayerVersionResponse> getAllLayerVersions(LambdaClient client, String layerName) {
        List<GetLayerVersionResponse> getLayerVersions = new ArrayList<>();
        ListLayerVersionsResponse versionsResponse = client.listLayerVersions(r -> r.layerName(layerName));
        List<Long> versions = versionsResponse.layerVersions().stream().map(LayerVersionsListItem::version).collect(Collectors.toList());
        for (Long version : versions) {
            getLayerVersions.add(client.getLayerVersion(r -> r.layerName(layerName).versionNumber(version)));
        }

        return getLayerVersions;
    }

    private boolean isValidLong(String val) {
        try {
            Long.parseLong(val);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }
}
