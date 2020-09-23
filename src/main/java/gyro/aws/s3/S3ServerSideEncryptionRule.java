/*
 * Copyright 2020, Perfect Sense, Inc.
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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionRule;

public class S3ServerSideEncryptionRule extends Diffable implements Copyable<ServerSideEncryptionRule> {

    private S3ServerSideEncryptionByDefault defaultEncryption;

    /**
     * The default server-side encryption to apply to new objects in the bucket.
     *
     * @subresource gyro.aws.s3.S3ServerSideEncryptionByDefault
     */
    @Required
    @Updatable
    public S3ServerSideEncryptionByDefault getDefaultEncryption() {
        return defaultEncryption;
    }

    public void setDefaultEncryption(S3ServerSideEncryptionByDefault defaultEncryption) {
        this.defaultEncryption = defaultEncryption;
    }

    @Override
    public void copyFrom(ServerSideEncryptionRule model) {
        S3ServerSideEncryptionByDefault encryptionByDefault = newSubresource(S3ServerSideEncryptionByDefault.class);
        encryptionByDefault.copyFrom(model.applyServerSideEncryptionByDefault());
        setDefaultEncryption(encryptionByDefault);
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ServerSideEncryptionRule toServerSideEncryptionRule() {
        return ServerSideEncryptionRule.builder().applyServerSideEncryptionByDefault(getDefaultEncryption().toServerSideEncryptionByDefault()).build();
    }
}
