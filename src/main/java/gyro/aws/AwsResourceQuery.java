package gyro.aws;

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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AwsResourceQuery<R extends AwsResource> extends ExternalResourceQuery<R> {

    protected List<Filter> createFilters(Map<String, String> query) {
        List<Filter> apiFilters = new ArrayList<>();
        for (String key : query.keySet()) {
            Filter filter = Filter.builder()
                .name(key)
                .values(query.get(key))
                .build();

            apiFilters.add(filter);
        }

        return apiFilters;
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
            Object value = field.getValue(this);
            String filterName = field.getFilterName();
            if (value instanceof Map) {
                Map valueMap = (Map) value;
                for (Object key : valueMap.keySet()) {
                    filters.put(String.format("%s:%s", filterName, key), valueMap.get(key).toString());
                }

            } else if (value != null) {
                filters.put(filterName, value.toString());
            }
        }

        return filters.isEmpty() ? queryAll() : query(filters);
    }
}
