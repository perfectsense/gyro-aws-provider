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
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionConfiguration;

public class S3ServerSideEncryptionConfiguration extends Diffable implements Copyable<ServerSideEncryptionConfiguration> {

    private S3ServerSideEncryptionRule encryptionRule;

    /**
     * The server-side encryption configuration rule.
     *
     * @subresource gyro.aws.s3.S3ServerSideEncryptionRule
     */
    @Required
    @Updatable
    public S3ServerSideEncryptionRule getEncryptionRule() {
        return encryptionRule;
    }

    public void setEncryptionRule(S3ServerSideEncryptionRule encryptionRule) {
        this.encryptionRule = encryptionRule;
    }

    @Override
    public void copyFrom(ServerSideEncryptionConfiguration model) {
        if (model.hasRules()) {
            S3ServerSideEncryptionRule rule = newSubresource(S3ServerSideEncryptionRule.class);
            rule.copyFrom(model.rules().get(0));
            setEncryptionRule(rule);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ServerSideEncryptionConfiguration toServerSideEncryptionConfiguration() {
        return ServerSideEncryptionConfiguration.builder().rules(getEncryptionRule().toServerSideEncryptionRule()).build();
    }
}
