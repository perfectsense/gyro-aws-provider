package gyro.aws.route53;

import com.psddev.dari.util.StringUtils;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class AliasTarget extends Diffable {

    private String dnsName;
    private String hostedZoneId;
    private Boolean evaluateTargetHealth;

    /**
     * Dns name to associate with this Record Set.
     */
    @Updatable
    public String getDnsName() {
        if (dnsName != null) {
            dnsName = StringUtils.ensureEnd(dnsName, ".");
        }

        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * The Hosted Zone where the 'dns name' belongs as configured.
     */
    @Updatable
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    /**
     * Enable target health evaluation with this Record Set.
     */
    @Updatable
    public Boolean getEvaluateTargetHealth() {
        return evaluateTargetHealth;
    }

    public void setEvaluateTargetHealth(Boolean evaluateTargetHealth) {
        this.evaluateTargetHealth = evaluateTargetHealth;
    }

    @Override
    public String primaryKey() {
        return getDnsName();
    }

}
