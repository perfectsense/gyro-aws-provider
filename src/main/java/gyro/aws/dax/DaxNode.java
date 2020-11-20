package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.Node;

public class DaxNode extends Diffable implements Copyable<Node> {

    private String availabilityZone;
    private DaxEndpoint endpoint;
    private String createTime;
    private String id;
    private String status;
    private DaxParameterGroupResource parameterGroupStatus;

    /**
     * The availability zone of the node.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The endpoint of the node.
     *
     * @subresource gyro.aws.dax.DaxEndpoint
     */
    public DaxEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(DaxEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The timestamp of when the node was created.
     */
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * The ID of the node.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The status of the node.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The status of the parameter group of the node.
     *
     * @subresource gyro.aws.dax.DaxParameterGroupResource
     */
    public DaxParameterGroupResource getParameterGroupStatus() {
        return parameterGroupStatus;
    }

    public void setParameterGroupStatus(DaxParameterGroupResource parameterGroupStatus) {
        this.parameterGroupStatus = parameterGroupStatus;
    }

    @Override
    public void copyFrom(Node model) {
        setAvailabilityZone(model.availabilityZone());
        setCreateTime(model.nodeCreateTime().toString());
        setId(model.nodeId());
        setStatus(model.nodeStatus());
        setParameterGroupStatus(findById(DaxParameterGroupResource.class, model.parameterGroupStatus()));

        if (model.endpoint() != null) {
            DaxEndpoint endpoint = newSubresource(DaxEndpoint.class);
            endpoint.copyFrom(model.endpoint());
            setEndpoint(endpoint);
        } else {
            setEndpoint(null);
        }
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getId());
    }
}
