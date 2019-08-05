package gyro.aws;

import gyro.core.FileBackend;
import gyro.core.Type;
import gyro.core.auth.Credentials;
import gyro.core.auth.CredentialsSettings;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

@Type("aws::s3")
public class S3FileBackend extends FileBackend {

    private String bucket;
    private  String prefix;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Stream<String> list() throws Exception {
        return null;
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        return client().getObject(r -> r.bucket(getBucket()).key(file));
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        return new OutputStream() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                sb.append(b);
            }

            public void close() {
                upload(getBucket(), file, sb.toString());
            }
        };
    }

    @Override
    public void delete(String file) throws Exception {
        client().deleteObject(r -> r.bucket(getBucket()).key(file));
    }

    private void upload(String bucket, String path, String content) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(path)
            .build();

        RequestBody body = RequestBody.fromString(content);

        client().putObject(request, body);
    }

    private S3Client client() {
        Credentials credentials = getRootScope().getSettings(CredentialsSettings.class)
            .getCredentialsByName()
            .get("aws::default");

        S3Client client = AwsResource.createClient(S3Client.class, (AwsCredentials) credentials);

        return client;
    }

}
