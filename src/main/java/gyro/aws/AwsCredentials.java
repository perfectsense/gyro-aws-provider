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

    @Override
    public void refresh() {
        provider().resolveCredentials();
    }

}
