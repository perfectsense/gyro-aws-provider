package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

import java.util.List;
import java.util.Map;

/**
 * Query key pair.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    key-pair: $(external-query aws::key-pair { key-name: ''})
 */
@Type("key-pair")
public class KeyPairFinder extends AwsFinder<Ec2Client, KeyPairInfo, KeyPairResource> {
    private String fingerprint;
    private String keyName;

    /**
     * The fingerprint of the key pair.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * The key name of the key pair.
     */
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    protected List<KeyPairInfo> findAllAws(Ec2Client client) {
        return client.describeKeyPairs().keyPairs();
    }

    @Override
    protected List<KeyPairInfo> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeKeyPairs(r -> r.filters(createFilters(filters))).keyPairs();
    }
}
