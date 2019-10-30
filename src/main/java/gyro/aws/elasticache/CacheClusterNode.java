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

package gyro.aws.elasticache;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.elasticache.model.CacheNode;

import java.util.Date;

public class CacheClusterNode extends Diffable implements Copyable<CacheNode> {

    private Date createTime;
    private String nodeId;
    private String status;
    private String availabilityZone;
    private String address;
    private Integer port;
    private String parameterGroupStatus;
    private String sourceNodeId;

    /**
     * The timestamp when creating the cache node.
     */
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * The Id of the cache node.
     */
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * The status of the cache node.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The availability zone of the cache node.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The endpoint address of the cache node.
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * The endpoint port of the cache node.
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The parameter group status of the cache node.
     */
    public String getParameterGroupStatus() {
        return parameterGroupStatus;
    }

    public void setParameterGroupStatus(String parameterGroupStatus) {
        this.parameterGroupStatus = parameterGroupStatus;
    }

    /**
     * The primary node Id for the cache node. This node is not associated with a primary cluster if empty.
     */
    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    @Override
    public void copyFrom(CacheNode model) {
        setCreateTime(Date.from(model.cacheNodeCreateTime()));
        setNodeId(model.cacheNodeId());
        setStatus(model.cacheNodeStatus());
        setAvailabilityZone(model.customerAvailabilityZone());
        setAddress(model.endpoint() != null ? model.endpoint().address() : null);
        setPort(model.endpoint() != null ? model.endpoint().port() : null);
        setParameterGroupStatus(model.parameterGroupStatus());
        setSourceNodeId(model.sourceCacheNodeId());
    }

    @Override
    public String primaryKey() {
        return getNodeId();
    }

}
