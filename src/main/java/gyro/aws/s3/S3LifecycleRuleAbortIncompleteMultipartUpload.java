/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.AbortIncompleteMultipartUpload;

public class S3LifecycleRuleAbortIncompleteMultipartUpload extends Diffable implements Copyable<AbortIncompleteMultipartUpload> {
    private Integer daysAfterInitiation;

    /**
     * Number of days after which incomplete multipart upload data be deleted.
     */
    @Updatable
    public Integer getDaysAfterInitiation() {
        return daysAfterInitiation;
    }

    public void setDaysAfterInitiation(Integer daysAfterInitiation) {
        this.daysAfterInitiation = daysAfterInitiation;
    }

    @Override
    public void copyFrom(AbortIncompleteMultipartUpload abortIncompleteMultipartUpload) {
        setDaysAfterInitiation(abortIncompleteMultipartUpload.daysAfterInitiation());
    }

    @Override
    public String primaryKey() {
        return "abort incomplete multipart abort";
    }

    AbortIncompleteMultipartUpload toAbortIncompleteMultipartUpload() {
        return AbortIncompleteMultipartUpload.builder().daysAfterInitiation(getDaysAfterInitiation()).build();
    }
}
