package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.BeamException;
import gyro.core.query.QueryField;
import gyro.core.query.QueryType;
import gyro.lang.ResourceFinder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsResourceFinder<C extends SdkClient, A, R extends AwsResource> extends ResourceFinder<R> {

    private SdkClient client;

    protected <T extends SdkClient> T createClient(Class<T> clientClass) {
        if (client == null) {
            client = AwsResource.createClient(clientClass, (AwsCredentials) credentials());
        }

        return (T) client;
    }

    protected List<Filter> createFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    public final List<R> query() {
        Map<String, String> filters = new HashMap<>();
        for (QueryField field : QueryType.getInstance(getClass()).getFields()) {
            String filterName = field.getFilterName();
            Object value = field.getValue(this);
            if (value instanceof Map) {
                Map<?, ?> valueMap = (Map<?, ?>) value;
                for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
                    filters.put(String.format("%s:%s", filterName, entry.getKey()), entry.getValue().toString());
                }

            } else if (value != null) {
                filters.put(filterName, value.toString());
            }
        }

        return filters.isEmpty() ? findAll() : find(filters);
    }

    protected abstract List<A> queryAws(C client, Map<String, String> filters);

    @Override
    public final List<R> find(Map<String, String> filters) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return queryAws(createClient(clientClass), filters).stream().map(this::createResource).collect(Collectors.toList());
    }

    protected abstract List<A> queryAllAws(C client);

    @Override
    public final List<R> findAll() {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return queryAllAws(createClient(clientClass)).stream().map(this::createResource).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private R createResource(A model) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        Class<A> modelClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 1);
        Class<R> resourceClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 2);

        try {
            return resourceClass.getConstructor(clientClass, modelClass).newInstance(createClient(clientClass), model);
        } catch (NoSuchMethodException nme) {
            throw new BeamException(String.format("No constructor %s(%s, %s) is defined!", resourceClass, clientClass, modelClass));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new BeamException(String.format("Unable to create resource of [%s]", resourceClass), e);
        }
    }
}
