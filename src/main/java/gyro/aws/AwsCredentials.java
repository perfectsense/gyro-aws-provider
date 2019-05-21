package gyro.aws;

import gyro.core.auth.Credentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class AwsCredentials extends Credentials<software.amazon.awssdk.auth.credentials.AwsCredentials> {

    private transient AwsCredentialsProvider provider;

    private String profileName;

    private String region;

    public AwsCredentials() {
        this.provider = AwsCredentialsProviderChain.builder()
                .credentialsProviders(DefaultCredentialsProvider.create())
                .build();
    }

    public AwsCredentialsProvider provider() {
        return provider;
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

    public void setProvider(AwsCredentialsProvider provider) {
        this.provider = provider;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public software.amazon.awssdk.auth.credentials.AwsCredentials findCredentials(boolean refresh) {
        return findCredentials(refresh, true);
    }

    @Override
    public software.amazon.awssdk.auth.credentials.AwsCredentials findCredentials(boolean refresh, boolean extended) {
        return provider().resolveCredentials();
    }

}
