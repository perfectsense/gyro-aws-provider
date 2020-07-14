package gyro.aws.wafv2;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.TextTransformation;

public class TextTransformationResource extends WafDiffable implements Copyable<TextTransformation> {

    private Integer priority;
    private String type;
    private Integer hashCode;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(TextTransformation textTransformation) {
        setPriority(textTransformation.priority());
        setType(textTransformation.typeAsString());
        setHashCode(textTransformation.hashCode());
    }

    TextTransformation toTextTransformation() {
        return TextTransformation.builder()
            .priority(getPriority())
            .type(getType())
            .build();
    }
}
