package gyro.aws.elbv2;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceName;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.FixedResponseActionConfig;

public class FixedResponseAction extends Diffable {

    private String contentType;
    private String messageBody;
    private String statusCode;

    public FixedResponseAction() {

    }

    public FixedResponseAction(FixedResponseActionConfig fixed) {
        setContentType(fixed.contentType());
        setMessageBody(fixed.messageBody());
        setStatusCode(fixed.statusCode());
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getContentType(), getMessageBody(), getStatusCode());
    }

    public String toDisplayString() {
        return "fixed response action";
    }

    public FixedResponseActionConfig toFixedAction() {
        return FixedResponseActionConfig.builder()
                .contentType(getContentType())
                .messageBody(getMessageBody())
                .statusCode(getStatusCode())
                .build();
    }
}
