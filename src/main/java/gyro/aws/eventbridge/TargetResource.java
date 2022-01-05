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

    @Required
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Required
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public BatchParameter getBatchParameters() {
        return batchParameters;
    }

    public void setBatchParameters(BatchParameter batchParameters) {
        this.batchParameters = batchParameters;
    }

    public SqsParameter getSqsParameters() {
        return sqsParameters;
    }

    public void setSqsParameters(SqsParameter sqsParameters) {
        this.sqsParameters = sqsParameters;
    }

    public DeadLetterConfig getDeadLetterConfig() {
        return deadLetterConfig;
    }

    public void setDeadLetterConfig(DeadLetterConfig deadLetterConfig) {
        this.deadLetterConfig = deadLetterConfig;
    }

    public InputTransformer getInputTransformer() {
        return inputTransformer;
    }

    public void setInputTransformer(InputTransformer inputTransformer) {
        this.inputTransformer = inputTransformer;
    }

    public KinesisParameter getKinesisParameters() {
        return kinesisParameters;
    }

    public void setKinesisParameters(KinesisParameter kinesisParameters) {
        this.kinesisParameters = kinesisParameters;
    }

    public RedshiftDataParameter getRedshiftDataParameters() {
        return redshiftDataParameters;
    }

    public void setRedshiftDataParameters(RedshiftDataParameter redshiftDataParameters) {
        this.redshiftDataParameters = redshiftDataParameters;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public RunCommandParameter getRunCommandParameters() {
        return runCommandParameters;
    }

    public void setRunCommandParameters(RunCommandParameter runCommandParameters) {
        this.runCommandParameters = runCommandParameters;
    }

    public SageMakerPipelineParameter getSageMakerPipelineParameters() {
        return sageMakerPipelineParameters;
    }

    public void setSageMakerPipelineParameters(SageMakerPipelineParameter sageMakerPipelineParameters) {
        this.sageMakerPipelineParameters = sageMakerPipelineParameters;
    }

    public HttpParameter getHttpParameters() {
        return httpParameters;
    }

    public void setHttpParameters(HttpParameter httpParameters) {
        this.httpParameters = httpParameters;
    }

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
