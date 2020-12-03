/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws;

import gyro.core.GyroException;
import gyro.core.resource.Resource;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsDefaultClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.conditions.RetryOnThrottlingCondition;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import java.lang.reflect.Method;
import java.net.URI;

public abstract class AwsResource extends Resource {

    private transient SdkClient client;

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

            ClientOverrideConfiguration.Builder retryPolicy = ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.builder()
                    .numRetries(20)
                    .retryCapacityCondition(RetryOnThrottlingCondition.create())
                    .build());

            Method method = clientClass.getMethod("builder");
            AwsDefaultClientBuilder builder = (AwsDefaultClientBuilder) method.invoke(null);
            builder.credentialsProvider(provider);
            builder.region(Region.of(region != null ? region : credentials.getRegion()));
            builder.httpClientBuilder(ApacheHttpClient.builder());
            builder.overrideConfiguration(retryPolicy.build());

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
