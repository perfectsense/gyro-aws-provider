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

import gyro.core.auth.Credentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class AwsCredentials extends Credentials {

    private String profileName;
    private String region;
    private AwsCredentialsProvider provider;
    private String clientConfig;

    public AwsCredentials() {
        this.provider = AwsCredentialsProviderChain.builder()
            .credentialsProviders(DefaultCredentialsProvider.create())
            .build();
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;

        this.provider = AwsCredentialsProviderChain.builder()
            .credentialsProviders(
                ProfileCredentialsProvider.create(profileName),
                DefaultCredentialsProvider.create()
            )
            .build();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public AwsCredentialsProvider provider() {
        return provider;
    }

    public String getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(String clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public void refresh() {
        provider().resolveCredentials();
    }

}
