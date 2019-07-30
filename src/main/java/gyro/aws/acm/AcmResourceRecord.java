package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.RecordType;
import software.amazon.awssdk.services.acm.model.ResourceRecord;

public class AcmResourceRecord extends Diffable implements Copyable<ResourceRecord> {
    private String name;
    private String value;
    private RecordType type;

    /**
     * The name of the DNS record to create in your domain.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the CNAME record to add to your DNS database.
     */
    @Output
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of DNS record.
     */
    @Output
    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ResourceRecord resourceRecord) {
        setName(resourceRecord.name());
        setType(resourceRecord.type());
        setValue(resourceRecord.value());
    }

    @Override
    public String primaryKey() {
        return "resource record";
    }
}
