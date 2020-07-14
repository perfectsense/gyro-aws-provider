package gyro.aws.wafv2;

import gyro.core.resource.Diffable;

public abstract class WafDiffable extends Diffable {

    private Integer hashCode;

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public String primaryKey() {
        return String.valueOf(getHashCode());
    }
}
