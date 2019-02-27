package gyro.aws;

import gyro.core.diff.ResourceName;
import gyro.lang.Credentials;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import java.util.Map;

@ResourceName("credentials")
public class AwsCredentials extends Credentials {

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

    @Override
    public String getCloudName() {
        return "aws";
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
    public Map<String, String> findCredentials(boolean refresh) {
        return findCredentials(refresh, true);
    }

    @Override
    public Map<String, String> findCredentials(boolean refresh, boolean extended) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        software.amazon.awssdk.auth.credentials.AwsCredentials creds;

        AwsCredentialsProvider provider = provider();
        creds = provider.resolveCredentials();

        mapBuilder.put("accessKeyId", creds.accessKeyId());
        mapBuilder.put("secretKey", creds.secretAccessKey());

        if (creds instanceof AwsSessionCredentials) {
            mapBuilder.put("sessionToken", ((AwsSessionCredentials) creds).sessionToken());
        }

        Long expiration = DateTime.now().plusDays(1).getMillis();
        mapBuilder.put("expiration", Long.toString(expiration));

        return mapBuilder.build();
    }

}
