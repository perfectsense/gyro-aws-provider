package gyro.aws.s3;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Type("bucket")
public class BucketFinder extends AwsFinder<S3Client, Bucket, BucketResource> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Bucket> findAllAws(S3Client client) {
        return client.listBuckets().buckets();
    }

    @Override
    protected List<Bucket> findAws(S3Client client, Map<String, String> filters) {
        List<Bucket> buckets = findAllAws(client);

        if (filters.containsKey("name")) {
            return buckets.stream().filter(o -> o.name().equals(filters.get("name"))).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
}
