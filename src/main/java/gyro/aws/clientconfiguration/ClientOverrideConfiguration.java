package gyro.aws.clientconfiguration;

import gyro.aws.clientconfiguration.retrypolicy.RetryPolicy;

public class ClientOverrideConfiguration implements ClientConfigurationInterface {

    private String apiCallTimeout;
    private RetryPolicy retryPolicy;

    public String getApiCallTimeout() {
        return apiCallTimeout;
    }

    public void setApiCallTimeout(String apiCallTimeout) {
        this.apiCallTimeout = apiCallTimeout;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public void validate() {
        if (getApiCallTimeout() != null) {
            ClientConfigurationUtils.validate(getApiCallTimeout(), "api-call-timeout", "client-override-configuration");
        }
    }

    public software.amazon.awssdk.core.client.config.ClientOverrideConfiguration toClientOverrideConfiguration() {
        software.amazon.awssdk.core.client.config.ClientOverrideConfiguration.Builder builder = software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
            .builder();

        if (getApiCallTimeout() != null) {
            builder.apiCallTimeout(ClientConfigurationUtils.getDuration(getApiCallTimeout()));
        }

        if (getRetryPolicy() != null) {
            builder.retryPolicy(getRetryPolicy().toRetryPolicy());
        }

        return builder.build();
    }
}
