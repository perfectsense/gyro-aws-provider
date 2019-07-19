package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.ExtendedKeyUsage;
import software.amazon.awssdk.services.acm.model.ExtendedKeyUsageName;

public class AcmExtendedKeyUsage extends Diffable implements Copyable<ExtendedKeyUsage> {
    private ExtendedKeyUsageName name;
    private String oid;

    @Output
    public ExtendedKeyUsageName getName() {
        return name;
    }

    public void setName(ExtendedKeyUsageName name) {
        this.name = name;
    }

    @Output
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Override
    public void copyFrom(ExtendedKeyUsage extendedKeyUsage) {
        setName(extendedKeyUsage.name());
        setOid(extendedKeyUsage.oid());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", (getName() != null ? getName().toString() : ""), getOid());
    }
}
