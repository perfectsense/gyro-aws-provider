package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.BeamException;
import gyro.core.query.QueryField;
import gyro.core.query.QueryType;
import gyro.lang.ExternalResourceQuery;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsDefaultClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsResourceQuery<C extends SdkClient, A, R extends AwsResource> extends ExternalResourceQuery<R> {

    protected List<Filter> createFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    private SdkClient client;

    protected  <T extends SdkClient> T createClient(Class<T> clientClass) {
        return createClient(clientClass, null, null);
    }

    protected <T extends SdkClient> T createClient(Class<T> clientClass, String region, String endpoint) {
        if (client != null) {
            return (T) client;
        }

        try {
            AwsCredentials credentials = (AwsCredentials) credentials();
            if (credentials == null) {
                throw new BeamException("No credentials associated with the resource.");
            }

            AwsCredentialsProvider provider = credentials.provider();

            Method method = clientClass.getMethod("builder");
            AwsDefaultClientBuilder builder = (AwsDefaultClientBuilder) method.invoke(null);
            builder.credentialsProvider(provider);
            builder.region(Region.of(region != null ? region : credentials.getRegion()));
            builder.httpClientBuilder(ApacheHttpClient.builder());

            if (endpoint != null) {
                builder.endpointOverride(URI.create(endpoint));
            }

            client = (T) builder.build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (T) client;
    }

    @Override
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

        return filters.isEmpty() ? queryAll() : query(filters);
    }

    protected abstract List<A> queryAws(C client, Map<String, String> filters);

    @Override
    public final List<R> query(Map<String, String> filters) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceQuery.class, 0);
        return queryAws(createClient(clientClass), filters).stream().map(this::createResource).collect(Collectors.toList());
    }

    protected abstract List<A> queryAllAws(C client);

    @Override
    public final List<R> queryAll() {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceQuery.class, 0);
        return queryAllAws(createClient(clientClass)).stream().map(this::createResource).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private R createResource(A model) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceQuery.class, 0);
        Class<A> modelClass = td.getInferredGenericTypeArgumentClass(AwsResourceQuery.class, 1);
        Class<R> resourceClass = td.getInferredGenericTypeArgumentClass(AwsResourceQuery.class, 2);

        try {
            return resourceClass.getConstructor(clientClass, modelClass).newInstance(createClient(clientClass), model);
        } catch (NoSuchMethodException nme) {
            throw new BeamException(String.format("No constructor %s(%s, %s) is defined!", resourceClass, clientClass, modelClass));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new BeamException(String.format("Unable to create resource of [%s]", resourceClass), e);
        }
    }
}
