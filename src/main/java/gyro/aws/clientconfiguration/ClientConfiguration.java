package gyro.aws.clientconfiguration;

public class ClientConfiguration implements ClientConfigurationInterface {

    private ClientOverrideConfiguration overrideConfiguration;
    private HttpClientConfiguration httpClientConfiguration;

    public ClientOverrideConfiguration getOverrideConfiguration() {
        return overrideConfiguration;
    }

    public void setOverrideConfiguration(ClientOverrideConfiguration overrideConfiguration) {
        this.overrideConfiguration = overrideConfiguration;
    }

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    public void setHttpClientConfiguration(HttpClientConfiguration httpClientConfiguration) {
        this.httpClientConfiguration = httpClientConfiguration;
    }

    @Override
    public void validate() {

    }
}
