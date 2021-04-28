/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.clientconfiguration;

import gyro.core.GyroException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

public class HttpClientConfiguration implements ClientConfigurationInterface {

    private String connectionAcquisitionTimeout;
    private String connectionTimeout;
    private String connectionTimeToLive;
    private String connectionMaxIdleTime;
    private Boolean expectContinueEnabled;
    private Integer maxConnections;
    private String socketTimeout;

    public String getConnectionAcquisitionTimeout() {
        return connectionAcquisitionTimeout;
    }

    public void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout) {
        this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
    }

    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(String connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public String getConnectionMaxIdleTime() {
        return connectionMaxIdleTime;
    }

    public void setConnectionMaxIdleTime(String connectionMaxIdleTime) {
        this.connectionMaxIdleTime = connectionMaxIdleTime;
    }

    public Boolean getExpectContinueEnabled() {
        return expectContinueEnabled;
    }

    public void setExpectContinueEnabled(Boolean expectContinueEnabled) {
        this.expectContinueEnabled = expectContinueEnabled;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    @Override
    public void validate() {
        if (getConnectionAcquisitionTimeout() != null) {
            ClientConfigurationUtils.validate(
                getConnectionAcquisitionTimeout(),
                "connection-acquisition-timeout",
                "http-client-configuration");
        }

        if (getConnectionTimeout() != null) {
            ClientConfigurationUtils.validate(
                getConnectionTimeout(),
                "connection-timeout",
                "http-client-configuration");
        }

        if (getConnectionTimeToLive() != null) {
            ClientConfigurationUtils.validate(
                getConnectionTimeToLive(),
                "connection-time-to-live",
                "http-client-configuration");
        }

        if (getConnectionMaxIdleTime() != null) {
            ClientConfigurationUtils.validate(
                getConnectionMaxIdleTime(),
                "connection-max-idle-time",
                "http-client-configuration");
        }

        if (getSocketTimeout() != null) {
            ClientConfigurationUtils.validate(getSocketTimeout(), "socket-timeout", "http-client-configuration");
        }

        if (getMaxConnections() != null && getMaxConnections() < 1) {
            throw new GyroException("'max-connections' if specified cannot be less than 1.");
        }
    }

    public ApacheHttpClient.Builder toApacheHttpClient() {
        ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

        if (getConnectionAcquisitionTimeout() != null) {
            builder.connectionAcquisitionTimeout(ClientConfigurationUtils.getDuration(getConnectionAcquisitionTimeout()));
        }

        if (getConnectionTimeout() != null) {
            builder.connectionTimeout(ClientConfigurationUtils.getDuration(getConnectionTimeout()));
        }

        if (getConnectionTimeToLive() != null) {
            builder = builder.connectionMaxIdleTime(ClientConfigurationUtils.getDuration(getConnectionTimeToLive()));
        }

        if (getConnectionMaxIdleTime() != null) {
            builder = builder.connectionMaxIdleTime(ClientConfigurationUtils.getDuration(getConnectionMaxIdleTime()));
        }

        if (getSocketTimeout() != null) {
            builder = builder.socketTimeout(ClientConfigurationUtils.getDuration(getSocketTimeout()));
        }

        if (getExpectContinueEnabled() != null) {
            builder = builder.expectContinueEnabled(getExpectContinueEnabled());
        }

        if (getMaxConnections() != null) {
            builder = builder.maxConnections(getMaxConnections());
        }

        return builder;
    }
}
