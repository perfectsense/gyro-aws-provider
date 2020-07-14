package gyro.aws.wafv2;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.IPSetReferenceStatement;

public class IpSetReferenceStatementResource extends WafDiffable implements Copyable<IPSetReferenceStatement> {

    private IpSetResource ipSet;

    public IpSetResource getIpSet() {
        return ipSet;
    }

    public void setIpSet(IpSetResource ipSet) {
        this.ipSet = ipSet;
    }

    @Override
    public void copyFrom(IPSetReferenceStatement ipSetReferenceStatement) {
        setIpSet(findById(IpSetResource.class, ipSetReferenceStatement.arn()));
        setHashCode(ipSetReferenceStatement.hashCode());
    }

    IPSetReferenceStatement toIpSetReferenceStatement() {
        return IPSetReferenceStatement.builder()
            .arn(getIpSet().getArn())
            .build();
    }
}
