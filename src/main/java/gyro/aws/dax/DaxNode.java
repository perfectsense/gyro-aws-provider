/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.model.Node;

public class DaxNode extends Diffable implements Copyable<Node> {

    private String availabilityZone;
    private DaxEndpoint endpoint;
    private String createTime;
    private String id;
    private String status;
    private String parameterGroupStatus;

    /**
     * The availability zone of the node.
     */
    @Output
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The endpoint configuration of the node.
     *
     * @subresource gyro.aws.dax.DaxEndpoint
     */
    @Output
    public DaxEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(DaxEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The timestamp of when the node was created.
     */
    @Output
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * The ID of the node.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The status of the node.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The status of the parameter group of the node.
     */
    @Output
    public String getParameterGroupStatus() {
        return parameterGroupStatus;
    }

    public void setParameterGroupStatus(String parameterGroupStatus) {
        this.parameterGroupStatus = parameterGroupStatus;
    }

    @Override
    public void copyFrom(Node model) {
        setAvailabilityZone(model.availabilityZone());
        setCreateTime(model.nodeCreateTime().toString());
        setId(model.nodeId());
        setStatus(model.nodeStatus());
        setParameterGroupStatus(model.parameterGroupStatus());

        setEndpoint(null);
        if (model.endpoint() != null) {
            DaxEndpoint endpoint = newSubresource(DaxEndpoint.class);
            endpoint.copyFrom(model.endpoint());
            setEndpoint(endpoint);
        }
    }

    @Override
    public String primaryKey() {
        return getId();
    }
}
