package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheParameterGroup;
import software.amazon.awssdk.services.elasticache.model.CacheParameterGroupNotFoundException;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheParameterGroupsResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheParametersResponse;
import software.amazon.awssdk.services.elasticache.model.DescribeEngineDefaultParametersResponse;
import software.amazon.awssdk.services.elasticache.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a cache parameter group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::elasticache-parameter-group cache-param-group-example
 *         name: "cache-param-group-example"
 *         cache-param-group-family: "redis5.0"
 *         description: "cache-param-group-desc"
 *
 *         parameters
 *             name: "activedefrag"
 *             value: "yes"
 *         end
 *     end
 */
@Type("elasticache-parameter-group")
public class CacheParameterGroupResource extends AwsResource implements Copyable<CacheParameterGroup> {
    private String name;
    private String cacheParamGroupFamily;
    private String description;
    private List<CacheParameter> parameters;

    // Internals
    private Set<String> configParamSet;

    /**
     * The name of the cache parameter group. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the cache parameter family. (Required)
     */
    public String getCacheParamGroupFamily() {
        return cacheParamGroupFamily;
    }

    public void setCacheParamGroupFamily(String cacheParamGroupFamily) {
        this.cacheParamGroupFamily = cacheParamGroupFamily;
    }

    /**
     * The description of the cache parameter group.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The list of cache parameter to modify.
     */
    @Updatable
    public List<CacheParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        return parameters;
    }

    public void setParameters(List<CacheParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public void copyFrom(CacheParameterGroup cacheParameterGroup) {
        setCacheParamGroupFamily(cacheParameterGroup.cacheParameterGroupFamily());
        setDescription(cacheParameterGroup.description());
        setName(cacheParameterGroup.cacheParameterGroupName());

        ElastiCacheClient client = createClient(ElastiCacheClient.class);
        DescribeCacheParametersResponse paramResponse = client.describeCacheParameters(
            r -> r.cacheParameterGroupName(getName())
        );

        configParamSet = getParameters().stream().map(CacheParameter::getName).collect(Collectors.toSet());
        getParameters().clear();
        for (Parameter parameter : paramResponse.parameters()) {
            if (parameter.isModifiable()) {
                getParameters().add(new CacheParameter(parameter.parameterName(), parameter.parameterValue()));
            }
        }
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CacheParameterGroup cacheParameterGroup = getCacheParameterGroup(client);

        if (cacheParameterGroup == null) {
            return false;
        }

        copyFrom(cacheParameterGroup);
        removeDefaultParams(getParameters(), configParamSet);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.createCacheParameterGroup(
            r -> r.description(getDescription())
                .cacheParameterGroupFamily(getCacheParamGroupFamily())
                .cacheParameterGroupName(getName())
        );

        saveParameters(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        saveParameters(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheParameterGroup(
            r -> r.cacheParameterGroupName(getName())
        );
    }

    private void removeDefaultParams(List<CacheParameter> parameters, Set<String> configParamSet) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        DescribeEngineDefaultParametersResponse defaultParamResponse = client.describeEngineDefaultParameters(
            r -> r.cacheParameterGroupFamily(getCacheParamGroupFamily())
        );

        Map<String, String> parameterMap = parameters.stream().filter(o -> !configParamSet.contains(o.getName())).collect(Collectors.toMap(CacheParameter::getName, o -> (o.getValue() == null ? "" : o.getValue())));

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
            r -> r.cacheParameterGroupName(getName())
        );

        Map<String, String> currentParamMap = paramResponse.parameters()
            .stream().filter(Parameter::isModifiable)
            .collect(Collectors.toMap(Parameter::parameterName, o -> (o.parameterValue() == null ? "" : o.parameterValue())));

        List<CacheParameter> modifiedParameters = getParameters().stream()
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

        Set<String> paramSet = getParameters().stream().map(CacheParameter::getName).collect(Collectors.toSet());

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
                    r -> r.cacheParameterGroupName(getName())
                        .parameterNameValues(parameters.stream().map(CacheParameter::getParameterNameValue).collect(Collectors.toList()))
                );
                start = start + max;
            } else {
                done = true;
            }
        } while (!done);
    }

    private CacheParameterGroup getCacheParameterGroup(ElastiCacheClient client) {
        CacheParameterGroup cacheParameterGroup = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load cache parameter group.");
        }

        try {
            DescribeCacheParameterGroupsResponse response = client.describeCacheParameterGroups(
                r -> r.cacheParameterGroupName(getName())
            );

            if (!response.cacheParameterGroups().isEmpty()) {
                cacheParameterGroup = response.cacheParameterGroups().get(0);
            }

        } catch (CacheParameterGroupNotFoundException ex) {
            // Ignore
        }

        return cacheParameterGroup;
    }
}
