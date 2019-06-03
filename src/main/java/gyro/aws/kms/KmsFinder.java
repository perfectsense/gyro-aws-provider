package gyro.aws.kms;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.KeyListEntry;
import software.amazon.awssdk.services.kms.model.KeyMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("kms")
public class KmsFinder extends AwsFinder<KmsClient, KeyMetadata, KmsResource> {

    private String keyId;

    /**
     * The id associated with the key.
     */
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @Override
    protected List<KeyMetadata> findAws(KmsClient client, Map<String, String> filters) {
        List<KeyMetadata> keys = new ArrayList<>();

        keys.add(client.describeKey(r -> r.keyId(filters.get("key-id"))).keyMetadata());

        return keys;
    }

    @Override
    protected List<KeyMetadata> findAllAws(KmsClient client) {
        List<KeyMetadata> keys = new ArrayList<>();

        for (KeyListEntry keyListEntry : client.listKeys().keys()) {
            keys.add(client.describeKey(r -> r.keyId(keyListEntry.keyId())).keyMetadata());
        }

        return keys;
    }
}
