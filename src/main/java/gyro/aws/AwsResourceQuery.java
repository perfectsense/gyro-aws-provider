package gyro.aws;

import gyro.core.BeamException;
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
import java.util.List;
import java.util.Map;

public abstract class AwsResourceQuery<R extends AwsResource> extends ExternalResourceQuery<R> {

    protected List<Filter> queryFilters(Map<String, String> query) {
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
}
