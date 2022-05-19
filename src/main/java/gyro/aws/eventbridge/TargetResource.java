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
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.eventbridge.model.Target;

public class TargetResource extends Diffable implements Copyable<Target> {

    private String arn;
    private String id;
    private String input;
    private String inputPath;
    private BatchParameter batchParameters;
    private SqsParameter sqsParameters;
    private DeadLetterConfig deadLetterConfig;
    private InputTransformer inputTransformer;
    private KinesisParameter kinesisParameters;
    private RedshiftDataParameter redshiftDataParameters;
    private RetryPolicy retryPolicy;
    private RunCommandParameter runCommandParameters;
    private SageMakerPipelineParameter sageMakerPipelineParameters;
    private HttpParameter httpParameters;
    private EcsParameter ecsParameters;

    /**
     * The source arn for the target.
     */
    @Required
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A unique id for the target.
     */
    @Required
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Valid JSON text passed to the target.
     */
    @Updatable
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    /**
     * The value of the JSONPath that is used for extracting part of the matched event when passing it to the target.
     */
    @Updatable
    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    /**
     * Batch parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.BatchParameter
     */
    @Updatable
    public BatchParameter getBatchParameters() {
        return batchParameters;
    }

    public void setBatchParameters(BatchParameter batchParameters) {
        this.batchParameters = batchParameters;
    }

    /**
     * Sqs parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.SqsParameter
     */
    @Updatable
    public SqsParameter getSqsParameters() {
        return sqsParameters;
    }

    public void setSqsParameters(SqsParameter sqsParameters) {
        this.sqsParameters = sqsParameters;
    }

    /**
     * Dead letter config for the target.
     *
     * @subresource gyro.aws.eventbridge.DeadLetterConfig
     */
    @Updatable
    public DeadLetterConfig getDeadLetterConfig() {
        return deadLetterConfig;
    }

    public void setDeadLetterConfig(DeadLetterConfig deadLetterConfig) {
        this.deadLetterConfig = deadLetterConfig;
    }

    /**
     * Input transformer for the target.
     *
     * @subresource gyro.aws.eventbridge.InputTransformer
     */
    @Updatable
    public InputTransformer getInputTransformer() {
        return inputTransformer;
    }

    public void setInputTransformer(InputTransformer inputTransformer) {
        this.inputTransformer = inputTransformer;
    }

    /**
     * Kinesis parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.KinesisParameter
     */
    @Updatable
    public KinesisParameter getKinesisParameters() {
        return kinesisParameters;
    }

    public void setKinesisParameters(KinesisParameter kinesisParameters) {
        this.kinesisParameters = kinesisParameters;
    }

    /**
     * Redshift data parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.RedshiftDataParameter
     */
    @Updatable
    public RedshiftDataParameter getRedshiftDataParameters() {
        return redshiftDataParameters;
    }

    public void setRedshiftDataParameters(RedshiftDataParameter redshiftDataParameters) {
        this.redshiftDataParameters = redshiftDataParameters;
    }

    /**
     * The retry policy for the target.
     *
     * @subresource gyro.aws.eventbridge.RetryPolicy
     */
    @Updatable
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * Run command parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.RunCommandParameter
     */
    @Updatable
    public RunCommandParameter getRunCommandParameters() {
        return runCommandParameters;
    }

    public void setRunCommandParameters(RunCommandParameter runCommandParameters) {
        this.runCommandParameters = runCommandParameters;
    }

    /**
     * Sagemaker pipeline parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.SageMakerPipelineParameter
     */
    @Updatable
    public SageMakerPipelineParameter getSageMakerPipelineParameters() {
        return sageMakerPipelineParameters;
    }

    public void setSageMakerPipelineParameters(SageMakerPipelineParameter sageMakerPipelineParameters) {
        this.sageMakerPipelineParameters = sageMakerPipelineParameters;
    }

    /**
     * Http parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.HttpParameter
     */
    @Updatable
    public HttpParameter getHttpParameters() {
        return httpParameters;
    }

    public void setHttpParameters(HttpParameter httpParameters) {
        this.httpParameters = httpParameters;
    }

    /**
     * Ecs parameter config for the target.
     *
     * @subresource gyro.aws.eventbridge.EcsParameter
     */
    @Updatable
    public EcsParameter getEcsParameters() {
        return ecsParameters;
    }

    public void setEcsParameters(EcsParameter ecsParameters) {
        this.ecsParameters = ecsParameters;
    }

    @Override
    public void copyFrom(Target model) {

        setArn(model.arn());
        setId(model.id());
        setInput(model.input());
        setInputPath(model.inputPath());

        BatchParameter batchParameter = null;
        if (model.batchParameters() != null) {
            batchParameter = newSubresource(BatchParameter.class);
            batchParameter.copyFrom(model.batchParameters());
        }
        setBatchParameters(batchParameter);

        SqsParameter sqsParameter = null;
        if (model.sqsParameters() != null) {
            sqsParameter = newSubresource(SqsParameter.class);
            sqsParameter.copyFrom(model.sqsParameters());
        }
        setSqsParameters(sqsParameter);

        DeadLetterConfig deadLetterConfig = null;
        if (model.deadLetterConfig() != null) {
            deadLetterConfig = newSubresource(DeadLetterConfig.class);
            deadLetterConfig.copyFrom(model.deadLetterConfig());
        }
        setDeadLetterConfig(deadLetterConfig);

        InputTransformer inputTransformer = null;
        if (model.inputTransformer() != null) {
            inputTransformer = newSubresource(InputTransformer.class);
            inputTransformer.copyFrom(model.inputTransformer());
        }
        setInputTransformer(inputTransformer);

        KinesisParameter kinesisParameter = null;
        if (model.kinesisParameters() != null) {
            kinesisParameter = newSubresource(KinesisParameter.class);
            kinesisParameter.copyFrom(model.kinesisParameters());
        }
        setKinesisParameters(kinesisParameter);

        RedshiftDataParameter redshiftDataParameter = null;
        if (model.redshiftDataParameters() != null) {
            redshiftDataParameter = newSubresource(RedshiftDataParameter.class);
            redshiftDataParameter.copyFrom(model.redshiftDataParameters());
        }
        setRedshiftDataParameters(redshiftDataParameter);

        RetryPolicy retryPolicy = null;
        if (model.retryPolicy() != null) {
            retryPolicy = newSubresource(RetryPolicy.class);
            retryPolicy.copyFrom(model.retryPolicy());
        }
        setRetryPolicy(retryPolicy);

        RunCommandParameter runCommandParameter = null;
        if (model.runCommandParameters() != null) {
            runCommandParameter = newSubresource(RunCommandParameter.class);
            runCommandParameter.copyFrom(model.runCommandParameters());
        }
        setRunCommandParameters(runCommandParameter);

        SageMakerPipelineParameter sageMakerPipelineParameter = null;
        if (model.sageMakerPipelineParameters() != null) {
            sageMakerPipelineParameter = newSubresource(SageMakerPipelineParameter.class);
            sageMakerPipelineParameter.copyFrom(model.sageMakerPipelineParameters());
        }
        setSageMakerPipelineParameters(sageMakerPipelineParameter);

        HttpParameter httpParameter = null;
        if (model.httpParameters() != null) {
            httpParameter = newSubresource(HttpParameter.class);
            httpParameter.copyFrom(model.httpParameters());
        }
        setHttpParameters(httpParameter);

        EcsParameter ecsParameter = null;
        if (model.ecsParameters() != null) {
            ecsParameter = newSubresource(EcsParameter.class);
            ecsParameter.copyFrom(model.ecsParameters());
        }
        setEcsParameters(ecsParameter);

    }

    @Override
    public String primaryKey() {
        return String.format("Source: %s, Id: %s", getArn(), getId());
    }

    protected Target toTarget() {
        Target.Builder builder = Target.builder().arn(getArn()).id(getId());

        if (!StringUtils.isBlank(getInput())) {
            builder = builder.input(getInput());
        }

        if (!StringUtils.isBlank(getInputPath())) {
            builder = builder.inputPath(getInputPath());
        }

        if (getBatchParameters() != null) {
            builder = builder.batchParameters(getBatchParameters().toBatchParameters());
        }

        if (getSqsParameters() != null) {
            builder = builder.sqsParameters(getSqsParameters().toSqsParameters());
        }

        if (getDeadLetterConfig() != null) {
            builder = builder.deadLetterConfig(getDeadLetterConfig().toDeadLetterConfig());
        }

        if (getInputTransformer() != null) {
            builder = builder.inputTransformer(getInputTransformer().toInputTransformer());
        }

        if (getKinesisParameters() != null) {
            builder = builder.kinesisParameters(getKinesisParameters().toKinesisParameters());
        }

        if (getRedshiftDataParameters() != null) {
            builder = builder.redshiftDataParameters(getRedshiftDataParameters().toRedshiftDataParameters());
        }

        if (getRetryPolicy() != null) {
            builder = builder.retryPolicy(getRetryPolicy().toRetryPolicy());
        }

        if (getRunCommandParameters() != null) {
            builder = builder.runCommandParameters(getRunCommandParameters().toRunCommandParameters());
        }

        if (getSageMakerPipelineParameters() != null) {
            builder = builder.sageMakerPipelineParameters(getSageMakerPipelineParameters().toSageMakerPipelineParameters());
        }

        if (getHttpParameters() != null) {
            builder = builder.httpParameters(getHttpParameters().toHttpParameters());
        }

        if (getEcsParameters() != null) {
            builder = builder.ecsParameters(getEcsParameters().toEcsParameters());
        }

        return builder.build();
    }
}
