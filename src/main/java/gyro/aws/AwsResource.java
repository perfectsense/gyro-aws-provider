package gyro.aws;

import gyro.core.GyroException;
import gyro.core.resource.Resource;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsDefaultClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import java.lang.reflect.Method;
import java.net.URI;

public abstract class AwsResource extends Resource {

    private SdkClient client;

    protected <T extends SdkClient> T createClient(Class<T> clientClass) {
        return createClient(clientClass, null, null);
    }

    protected <T extends SdkClient> T createClient(Class<T> clientClass, String region, String endpoint) {
        AwsCredentials credentials = (AwsCredentials) resourceCredentials();
        if (credentials == null) {
            throw new GyroException("No credentials associated with the resource.");
        }

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
