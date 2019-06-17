package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.FixedResponseActionConfig;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         type: “fixed-response”
 *
 *         fixed-response-action
 *             content-type: "text/plain"
 *             message-body: "fixed response message"
 *             status-code: "200"
 *         end
 *     end
 */
public class FixedResponseAction extends Diffable implements Copyable<FixedResponseActionConfig> {

    private String contentType;
    private String messageBody;
    private String statusCode;

    /**
     *  The content type. Valid values are ``text/plain``, ``text/css``, ``text/html``, ``application/javascript`` and ``application/json``. (Required)
     */
    @Updatable
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     *  The message. (Required)
     */
    @Updatable
    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     *  The status code. Valid values are 2XX, 4XX, or 5XX. (Required)
     */
    @Updatable
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getContentType(), getMessageBody(), getStatusCode());
    }

    @Override
    public void copyFrom(FixedResponseActionConfig fixed) {
        setContentType(fixed.contentType());
        setMessageBody(fixed.messageBody());
        setStatusCode(fixed.statusCode());
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
