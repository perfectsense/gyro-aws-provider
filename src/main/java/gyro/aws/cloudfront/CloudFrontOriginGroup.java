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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.OriginGroup;
import software.amazon.awssdk.services.cloudfront.model.OriginGroupFailoverCriteria;
import software.amazon.awssdk.services.cloudfront.model.OriginGroupMembers;

public class CloudFrontOriginGroup extends Diffable implements Copyable<OriginGroup> {

    private String id;
    private CloudFrontOriginGroupFailoverCriteria failoverCriteria;
    private List<CloudFrontOriginGroupMember> members;

    /**
     * A unique ID for this origin group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The failover criteria for this origin group.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontOriginGroupFailoverCriteria
     */
    @Updatable
    public CloudFrontOriginGroupFailoverCriteria getFailoverCriteria() {
        return failoverCriteria;
    }

    public void setFailoverCriteria(CloudFrontOriginGroupFailoverCriteria failoverCriteria) {
        this.failoverCriteria = failoverCriteria;
    }

    /**
     * The members of this origin group.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontOriginGroupMember
     */
    @Updatable
    public List<CloudFrontOriginGroupMember> getMember() {
        if (members == null) {
            members = new ArrayList<>();
        }

        return members;
    }

    public void setMember(List<CloudFrontOriginGroupMember> members) {
        this.members = members;
    }

    // Internal method for consistency - not exposed to gyro
    List<CloudFrontOriginGroupMember> getMembers() {
        return getMember();
    }

    @Override
    public void copyFrom(OriginGroup originGroup) {
        setId(originGroup.id());

        if (originGroup.failoverCriteria() != null) {
            CloudFrontOriginGroupFailoverCriteria failoverCriteria = newSubresource(CloudFrontOriginGroupFailoverCriteria.class);
            failoverCriteria.copyFrom(originGroup.failoverCriteria());
            setFailoverCriteria(failoverCriteria);
        }

        getMembers().clear();
        if (originGroup.members() != null && originGroup.members().items() != null) {
            for (software.amazon.awssdk.services.cloudfront.model.OriginGroupMember member : originGroup.members().items()) {
                CloudFrontOriginGroupMember originGroupMember = newSubresource(CloudFrontOriginGroupMember.class);
                originGroupMember.copyFrom(member);
                getMembers().add(originGroupMember);
            }
        }
    }

    @Override
    public String primaryKey() {
        return getId();
    }

    OriginGroup toOriginGroup() {
        List<software.amazon.awssdk.services.cloudfront.model.OriginGroupMember> members = getMembers()
            .stream()
            .map(CloudFrontOriginGroupMember::toOriginGroupMember)
            .collect(Collectors.toList());

        OriginGroupMembers originGroupMembers = OriginGroupMembers.builder()
            .items(members)
            .quantity(members.size())
            .build();

        return OriginGroup.builder()
            .id(getId())
            .failoverCriteria(getFailoverCriteria() != null ? getFailoverCriteria().toOriginGroupFailoverCriteria() : null)
            .members(originGroupMembers)
            .build();
    }
}