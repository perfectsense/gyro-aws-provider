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

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public DaxEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(DaxEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
