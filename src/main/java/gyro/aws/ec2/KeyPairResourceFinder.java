package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

import java.util.List;
import java.util.Map;

@Type("key-pair")
public class KeyPairResourceFinder extends AwsFinder<Ec2Client, KeyPairInfo, KeyPairResource> {
    private String fingerprint;
    private String keyName;

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

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
