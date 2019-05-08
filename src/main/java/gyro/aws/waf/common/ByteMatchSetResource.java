package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceUpdatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ByteMatchSetResource extends AbstractWafResource {
    private String name;
    private String byteMatchSetId;

    /**
     * The name of the byte match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getByteMatchSetId() {
        return byteMatchSetId;
    }

    public void setByteMatchSetId(String byteMatchSetId) {
        this.byteMatchSetId = byteMatchSetId;
    }



    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("byte match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getByteMatchSetId())) {
            sb.append(" - ").append(getByteMatchSetId());
        }

        return sb.toString();
    }
}
