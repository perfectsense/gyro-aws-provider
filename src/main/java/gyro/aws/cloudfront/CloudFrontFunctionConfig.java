/*
 * Copyright 2023, Brightspot.
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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.model.FunctionConfig;
import software.amazon.awssdk.services.cloudfront.model.FunctionRuntime;

public class CloudFrontFunctionConfig extends Diffable implements Copyable<FunctionConfig> {

    private String comment;
    private FunctionRuntime runtime;

    /**
     * A comment to describe the function.
     */
    @Updatable
    public String getComment() {
        if (comment == null) {
            comment = "";
        }

        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The runtime environment for the function. Defaults to ``cloudfront-js-1.0``.
     */
    @ValidStrings("cloudfront-js-1.0")
    @Updatable
    public FunctionRuntime getRuntime() {
        if (runtime == null) {
            runtime = FunctionRuntime.CLOUDFRONT_JS_1_0;
        }

        return runtime;
    }

    public void setRuntime(FunctionRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(FunctionConfig model) {
        setComment(model.comment());
        setRuntime(model.runtime());
    }

    FunctionConfig toFunctionConfig() {
        return FunctionConfig.builder()
            .comment(getComment())
            .runtime(getRuntime())
            .build();
    }
}
