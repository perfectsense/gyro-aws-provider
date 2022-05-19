/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.BatchParameters;

public class BatchParameter extends Diffable implements Copyable<BatchParameters> {

    private BatchRetryStrategy retryStrategy;
    private BatchArrayProperty arrayProperties;
    private String jobDefinition;
    private String jobName;

    /**
     * The batch retry strategy config for the batch parameter.
     *
     * @subresource gyro.aws.eventbridge.BatchRetryStrategy
     */
    @Updatable
    public BatchRetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(BatchRetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    /**
     * The batch array property config for the batch parameter.
     *
     * @subresource gyro.aws.eventbridge.BatchArrayProperty
     */
    @Updatable
    public BatchArrayProperty getArrayProperties() {
        return arrayProperties;
    }

    public void setArrayProperties(BatchArrayProperty arrayProperties) {
        this.arrayProperties = arrayProperties;
    }

    /**
     * The job definition for the batch parameter.
     */
    @Required
    @Updatable
    public String getJobDefinition() {
        return jobDefinition;
    }

    public void setJobDefinition(String jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    /**
     * The job name for the batch parameter.
     */
    @Required
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public void copyFrom(BatchParameters model) {
        BatchRetryStrategy retryStrategy = null;
        if (model.retryStrategy() != null) {
            retryStrategy = newSubresource(BatchRetryStrategy.class);
            retryStrategy.copyFrom(model.retryStrategy());
        }
        setRetryStrategy(retryStrategy);

        BatchArrayProperty arrayProperty = null;
        if (model.arrayProperties() != null) {
            arrayProperty = newSubresource(BatchArrayProperty.class);
            arrayProperty.copyFrom(model.arrayProperties());
        }
        setArrayProperties(arrayProperty);

        setJobDefinition(model.jobDefinition());
        setJobName(model.jobName());
    }

    @Override
    public String primaryKey() {
        return getJobName();
    }

    protected BatchParameters toBatchParameters() {
        BatchParameters.Builder builder = BatchParameters.builder()
            .jobName(getJobName())
            .jobDefinition(getJobDefinition());

        if (getArrayProperties() != null) {
            builder.arrayProperties(getArrayProperties().toBatchArrayProperties());
        }

        if (getRetryStrategy() != null) {
            builder.retryStrategy(getRetryStrategy().toBatchRetryStrategy());
        }

        return builder.build();
    }
}
