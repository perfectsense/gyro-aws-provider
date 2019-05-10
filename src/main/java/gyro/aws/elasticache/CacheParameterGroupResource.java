package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheParameterGroup;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheParameterGroupsResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheParametersResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeEngineDefaultParametersResponse;
import software.amazon.awssdk.services.elasticache.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceType("cache-param-group")
public class CacheParameterGroupResource extends AwsResource {
    private String cacheParamGroupName;
    private String cacheParamGroupFamily;
    private String description;
    private List<CacheParameter> parameters;

    public String getCacheParamGroupName() {
        return cacheParamGroupName;
    }

    public void setCacheParamGroupName(String cacheParamGroupName) {
        this.cacheParamGroupName = cacheParamGroupName;
    }

    public String getCacheParamGroupFamily() {
        return cacheParamGroupFamily;
    }

    public void setCacheParamGroupFamily(String cacheParamGroupFamily) {
        this.cacheParamGroupFamily = cacheParamGroupFamily;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ResourceUpdatable
    public List<CacheParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        removeDefaultParams(parameters);

        return parameters;
    }

    private List<CacheParameter> getParametersWithoutReset() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        return parameters;
    }

    public void setParameters(List<CacheParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeCacheParameterGroupsResponse response = client.describeCacheParameterGroups(
            r -> r.cacheParameterGroupName(getCacheParamGroupName())
        );

        if (!response.cacheParameterGroups().isEmpty()) {
            CacheParameterGroup cacheParameterGroup = response.cacheParameterGroups().get(0);
            setCacheParamGroupFamily(cacheParameterGroup.cacheParameterGroupFamily());
            setDescription(cacheParameterGroup.description());

            DescribeCacheParametersResponse paramResponse = client.describeCacheParameters(
                r -> r.cacheParameterGroupName(getCacheParamGroupName())
            );

            getParametersWithoutReset().clear();
            for (Parameter parameter : paramResponse.parameters()) {
                if (parameter.isModifiable()) {
                    getParametersWithoutReset().add(new CacheParameter(parameter.parameterName(), parameter.parameterValue()));
                }
            }
            removeDefaultParams(getParametersWithoutReset());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void create() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.createCacheParameterGroup(
            r -> r.description(getDescription())
                .cacheParameterGroupFamily(getCacheParamGroupFamily())
                .cacheParameterGroupName(getCacheParamGroupName())
        );

        saveParameters(client);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        saveParameters(client);
    }

    @Override
    public void delete() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheParameterGroup(
            r -> r.cacheParameterGroupName(getCacheParamGroupName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("cache param group");

        if (!ObjectUtils.isBlank(getCacheParamGroupName())) {
            sb.append(" - ").append(getCacheParamGroupName());
        }

        return sb.toString();
    }

    private void removeDefaultParams(List<CacheParameter> parameters) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeEngineDefaultParametersResponse defaultParamResponse = client.describeEngineDefaultParameters(
            r -> r.cacheParameterGroupFamily(getCacheParamGroupFamily())
        );

        Map<String, String> parameterMap = parameters.stream().collect(Collectors.toMap(CacheParameter::getName, o -> (o.getValue() == null ? "" : o.getValue())));

        Set<String> defaultParameterKeySet = defaultParamResponse.engineDefaults().parameters()
            .stream()
            .filter(
                f -> f.isModifiable()
                    && parameterMap.containsKey(f.parameterName())
                    && (parameterMap.get(f.parameterName()).equals(f.parameterValue())
                    || (ObjectUtils.isBlank(parameterMap.get(f.parameterName())) && ObjectUtils.isBlank(f.parameterValue())))
            ).map(Parameter::parameterName).collect(Collectors.toSet());

        parameters.removeIf(o -> defaultParameterKeySet.contains(o.getName()));
    }

    private void saveParameters(ElastiCacheClient client) {

        //Add modified params
        DescribeCacheParametersResponse paramResponse = client.describeCacheParameters(
            r -> r.cacheParameterGroupName(getCacheParamGroupName())
        );

        Map<String, String> currentParamMap = paramResponse.parameters()
            .stream().filter(Parameter::isModifiable)
            .collect(Collectors.toMap(Parameter::parameterName, o -> (o.parameterValue() == null ? "" : o.parameterValue())));

        List<CacheParameter> modifiedParameters = getParametersWithoutReset().stream()
            .filter(
                o -> currentParamMap.containsKey(o.getName()) && !currentParamMap.get(o.getName()).equals(o.getValue())
            )
            .collect(Collectors.toList());

        // Add removed params that have been modified
        DescribeEngineDefaultParametersResponse defaultParamResponse = client.describeEngineDefaultParameters(
            r -> r.cacheParameterGroupFamily(getCacheParamGroupFamily())
        );

        Map<String, String> defaultParamMap = defaultParamResponse.engineDefaults().parameters()
            .stream().filter(Parameter::isModifiable)
            .collect(Collectors.toMap(Parameter::parameterName, o -> (o.parameterValue() == null ? "" : o.parameterValue())));

        Set<String> paramSet = getParametersWithoutReset().stream().map(CacheParameter::getName).collect(Collectors.toSet());

        modifiedParameters.addAll(defaultParamMap.keySet().stream()
            .filter(o -> paramSet.isEmpty() || !paramSet.contains(o))
            .map(o -> new CacheParameter(o, defaultParamMap.get(o)))
            .collect(Collectors.toList()));

        // divide the list into max 20 item chunks
        // The api can handle max 20 param modification at a time.
        int start = 0;
        int max = 20;
        boolean done = false;
        do {
            int end = modifiedParameters.size() < (start + max) ? modifiedParameters.size() : (start + max);
            List<CacheParameter> parameters = start > end ? new ArrayList<>() : modifiedParameters.subList(start, end);
            if (start < modifiedParameters.size() || !parameters.isEmpty()) {
                client.modifyCacheParameterGroup(
                    r -> r.cacheParameterGroupName(getCacheParamGroupName())
                        .parameterNameValues(parameters.stream().map(CacheParameter::getParameterNameValue).collect(Collectors.toList()))
                );
                start = start + max;
            } else {
                done = true;
            }
        } while (!done);
    }
}
