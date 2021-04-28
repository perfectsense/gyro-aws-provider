# AWS Client override configuration
The AWS **client-configuration** allows you to override the default client creation options.

You define the `client-configuration` directive in your `.init.gyro` file.

```
@aws::client-configuration <config-name>

@end
```

If no config name is given, it is treated as `default` and is used as the default override for all client creations.

```
@aws::client-configuration # default configuration
    override-configuration
        retry-policy
            count: 20

            capacity-retry-condition
                retry-on-throttling-condition

                end
            end
        end
    end
@end
```

Multiple client-configuration directives can be defined using different names. These can be associated with individual credential directives. In doing so all assets using that credential will create the aws client with overrides as defined by the mentioned client configuration.

```
@credentials 'enterprise::aws-credentials' 'east-2'
    enterprise-url: "https://beam-enterprise.psdops.com"
    project: $(project)
    account: $(account)
    region: "us-east-2"
    client-config: "us-east-2-config"
@end

@aws::client-configuration 'us-east-2-config'
    override-configuration
        retry-policy
            count: 30

            capacity-retry-condition
                retry-on-throttling-condition

                end
            end
        end
    end
@end

@aws::client-configuration 'us-west-2-config'
    http-client-configuration
        socket-timeout: "120S"
        max-connections: 20
    end
    
    override-configuration
        retry-policy
            count: 10

            capacity-retry-condition
                retry-on-throttling-condition

                end
            end
        end
    end
@end
```
In this case, all assets using the `east-2` credentials will create there respective aws clients using the overrides defined in the client-configuration named `east-2-config`.

## Configuration Details

The client-configuration has the following options:
- **http-client-configuration** - Http configuration for the client. [See options](#Http-Client-Configuration)
- **override-client-configuration** - Override configuration for the client. [See options](#Override-Client-Configuration)

### Http Client Configuration
- **socket-timeout** - The amount of [time](#Time) to wait for data to be transferred over an established, open connection before the connection is timed out. A duration of `0` means infinity, and is not recommended.
- **connection-timeout** - The amount of [time](#Time) to wait when initially establishing a connection before giving up and timing out. A duration of `0` means infinity, and is not recommended.
- **connection-acquisition-timeout** - The amount of [time](#Time) to wait when acquiring a connection from the pool before giving up and timing out.
- **max-connections** - The maximum number of connections allowed in the connection pool.
- **expect-continue-enabled** - Configure whether the client should send an HTTP expect-continue handshake before each request.
- **connection-time-to-live** - The maximum amount of [time](#Time) that a connection should be allowed to remain open, regardless of usage frequency.
- **connection-max-idle-time** - Configure the maximum amount of [time](#Time) that a connection should be allowed to remain open while idle.

### Override Client Configuration
- **api-call-timeout** - Specify the amount of [time](#Time) to allow the client to complete the execution of an API call. This timeout covers the entire client execution except for marshalling. This includes request handler execution, all HTTP requests including retries, unmarshalling, etc. This value should always be positive, if present.
- **retry-policy** - Configure the retry policy that should be used when handling failure cases.
    - **retry-count** - Specify the maximum number of times that a single request should be retried, assuming it fails for a retryable error
    - **additional-retry-conditions-allowed** - Specify whether further conditions can be added to this policy after it is created.
    - **backoff-strategy** - Specify the backoff strategy that should be used for waiting in between retry attempts. If the retry is because of throttling reasons, the `throttling-backoff-strategy` is used instead. [See available Backoff Strategies](#Backoff-Strategies)
    - **throttling-backoff-strategy** - Specify the backoff strategy that should be used for waiting in between retry attempts after a throttling error is encountered. If the retry is not because of throttling reasons, the `backoff-strategy` is used instead. [See available Backoff Strategies](#Backoff-Strategies)
    - **retry-condition** - Specify the retry condition under which the request should be retried. [See available Retry Conditions](#Retry-Conditions)
    - **retry-capacity-condition** - Specify the retry condition that should be used to throttle the number of retries attempted by the SDK client as a whole. [See available Retry Conditions](#Retry-Conditions)

### Backoff Strategies
- **equal-jitter** - Backoff strategy that uses equal jitter for computing the delay before the next retry.
    - **base-delay** - The base delay to be used for the jitter calculation.
    - **max-backoff-time** - The max-backoff [time](#Time) to be used for the jitter calculation.
- **full-jitter** - Backoff strategy that uses a full jitter strategy for computing the next backoff delay.
    - **base-delay** - The base delay to be used for the jitter calculation.
    - **max-backoff-time** - The max-backoff [time](#Time) to be used for the jitter calculation.
- **fixed-backoff** - Simple backoff strategy that always uses a fixed delay for the delay before the next retry attempt.
    - **delay** - The delay between the retry attempts.

### Retry Conditions
- **and-retry-condition** - Composite retry condition that evaluates to true when all contained retry conditions evaluate to true.
    - A list of retry conditions that are checked for all to be true to satisfy the retry condition.
- **or-retry-condition** - Composite retry condition that evaluates to true if any containing condition evaluates to true.
    - A list of retry condition that are checked for any one of them to be true to satisfy teh condition.
- **retry-on-error-codes-condition** - Retry condition implementation that retries if the exception, or the cause of the exception matches the error codes defined.
    - **retryable-error-codes** - A list of string specifying the error codes to match for retrying.
- **retry-on-status-codes-condition** - Retry condition implementation that retries if the HTTP status code matches one of the provided status codes.
    - **retryable-status-codes** - A list of integers specifying the error codes to match for retrying.
- **max-number-of-retry-condition** - Simple retry condition that allows retries up to a certain max number of retries.
    - **retry-count** - An integer specifying the max number of retries allowed.
- **retry-on-throttling-condition** - A retry condition that will return `true` if the provided exception seems to be due to a throttling error from the service to the client.

#### Time
Time is specified as a string with formats based on the ISO-8601 duration format `PnDTnHnMn.nS` with days considered to be exactly 24 hours.
[See here for more details.](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)
