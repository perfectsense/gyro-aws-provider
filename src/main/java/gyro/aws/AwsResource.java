package gyro.aws;

import gyro.core.GyroException;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.scope.DiffableScope;
import gyro.core.scope.RootScope;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsDefaultClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public abstract class AwsResource extends Resource {

    private SdkClient client;

    protected <T extends SdkClient> T createClient(Class<T> clientClass) {
        return createClient(clientClass, null, null);
    }

    @SuppressWarnings("unchecked")
    protected <T extends SdkClient> T createClient(Class<T> clientClass, String region, String endpoint) {
        AwsCredentials credentials = credentials(AwsCredentials.class);
        client = createClient(clientClass, credentials, region, endpoint);
        return (T) client;
    }

    public static <T extends SdkClient> T createClient(Class<T> clientClass, AwsCredentials credentials) {
        return createClient(clientClass, credentials, null, null);
    }

    public static <T extends SdkClient> T createClient(Class<T> clientClass, AwsCredentials credentials, String region, String endpoint) {

        try {
            if (credentials == null) {
                throw new GyroException(String.format("Unable to create %s, no credentials specified!", clientClass));
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

            return (T) builder.build();
        } catch (Exception ex) {
            throw new GyroException(String.format("Unable to create %s !", clientClass), ex);
        }
    }

    protected Map<String, String> getRootScopeTags(){
        Map<String, String> tags = new HashMap<>();
        DiffableScope scope = DiffableInternals.getScope(this);
        RootScope rootScope = scope.getRootScope();
        tags.put("project", (String) rootScope.get("project"));
        tags.put("account", (String) rootScope.get("account"));
        tags.put("serial", (String) rootScope.get("serial"));
        return tags;
    }

    @FunctionalInterface
    protected interface Service {
        Object apply();
    }

    public Object executeService(Service service) {
        boolean available = false;
        int counter = 10;
        Object result = null;
        while (!available) {
            available = true;
            try {
                result = service.apply();
            } catch (Exception error) {
                available = false;
                counter--;

                if (counter < 0) {
                    throw new GyroException("AWS service request failed!\n" + error.getMessage());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }

        return result;
    }

}
