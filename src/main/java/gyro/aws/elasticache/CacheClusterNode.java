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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getParameterGroupStatus() {
        return parameterGroupStatus;
    }

    public void setParameterGroupStatus(String parameterGroupStatus) {
        this.parameterGroupStatus = parameterGroupStatus;
    }

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

    @Override
    public String toDisplayString() {
        return "cache cluster node " + getNodeId();
    }
}
