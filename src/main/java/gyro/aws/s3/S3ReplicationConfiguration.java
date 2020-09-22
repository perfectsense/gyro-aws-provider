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
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.ReplicationConfiguration;
import software.amazon.awssdk.services.s3.model.ReplicationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S3ReplicationConfiguration extends Diffable implements Copyable<ReplicationConfiguration> {
    private RoleResource role;
    private List<S3ReplicationRule> rule;

    /**
     * The ARN for an IAM Role that the s3 bucket assumes when replicating objects. (Required)
     */
    @Required
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }


    /**
     * Configure cross region replication rules. (Required)
     *
     * @subresource gyro.aws.s3.ReplicationRule
     */
    @Required
    @Updatable
    public List<S3ReplicationRule> getRule() {
        if(rule == null) {
            rule = new ArrayList<>();
        }
        return rule;
    }

    public void setRule(List<S3ReplicationRule> rule) {
        this.rule = rule;
    }

    @Override
    public String primaryKey() {
        return "replication configuration";
    }

    @Override
    public void copyFrom(ReplicationConfiguration replicationConfiguration) {
        setRole(findById(RoleResource.class, replicationConfiguration.role()));

        getRule().clear();
        for (ReplicationRule replicationRule : replicationConfiguration.rules()) {
            S3ReplicationRule s3ReplicationRule = newSubresource(S3ReplicationRule.class);
            s3ReplicationRule.copyFrom(replicationRule);
            getRule().add(s3ReplicationRule);
        }
    }

    ReplicationConfiguration toReplicationConfiguration() {
        return ReplicationConfiguration.builder()
                .role(getRole().getArn())
                .rules(getRule().stream().map(S3ReplicationRule::toReplicationRule)
                        .collect(Collectors.toList()))
                .build();
    }
}
