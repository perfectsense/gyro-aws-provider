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
import software.amazon.awssdk.services.s3.model.ReplicationRuleAndOperator;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S3ReplicationRuleAndOperator extends Diffable implements Copyable<ReplicationRuleAndOperator> {
    private String prefix;
    private List<S3Tag> tag;

    /**
     * Object prefix that this rule applies to. (Required)
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * List of tags to select the objects that will be replicated (Required)
     *
     * @subresource aws.aws.s3.S3Tag
     */
    @Updatable
    public List<S3Tag> getTag() {
        if(tag == null){
            tag =  new ArrayList<>();
        }
        return tag;
    }

    public void setTag(List<S3Tag> tag) {
        this.tag = tag;
    }

    @Override
    public String primaryKey() {
        return "replication rule and operator";
    }

    @Override
    public void copyFrom(ReplicationRuleAndOperator replicationRuleAndOperator) {
        setPrefix(replicationRuleAndOperator.prefix());

        if (replicationRuleAndOperator.tags() != null) {
            for (Tag tag : replicationRuleAndOperator.tags()) {
                S3Tag s3tag = newSubresource(S3Tag.class);
                s3tag.copyFrom(tag);
                getTag().add(s3tag);
            }
        }
    }

    ReplicationRuleAndOperator toReplicationRuleAndOperator() {
        ReplicationRuleAndOperator.Builder builder = ReplicationRuleAndOperator.builder();
        builder.prefix(getPrefix());

        if (!getTag().isEmpty()) {
            builder.tags(getTag().stream().map(S3Tag::toTag).collect(Collectors.toList()));
        }

        return builder.build();
    }
}
