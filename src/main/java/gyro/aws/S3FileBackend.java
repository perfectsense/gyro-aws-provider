package gyro.aws;

import com.google.common.collect.ImmutableSet;
import gyro.core.FileBackend;
import gyro.core.NamespaceUtils;
import gyro.core.Type;

import gyro.core.auth.Credentials;
import gyro.core.auth.CredentialsSettings;

import gyro.core.scope.RootScope;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

@Type("aws::s3")
public class S3FileBackend extends FileBackend {

    private String bucket;
    private  String prefix;
    RootScope root;
    private String configPath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private String filePath;

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
        return null;
    }

    @Override
    public Set<String> getNameSpaces() {
        return ImmutableSet.of(NamespaceUtils.getNamespace(getClass()));
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {

        root = getRootScope();

        Credentials credentials = root.getSettings(CredentialsSettings.class)
                .getCredentialsByName()
                .get("aws::default");

        S3Client s3Client = AwsResource.createClient(S3Client.class, (AwsCredentials) credentials);

        S3AsyncClient client = AwsResource.createClient(S3AsyncClient.class, (AwsCredentials) credentials);

        StringBuilder sb = new StringBuilder();

        sb.append(getPrefix()).append(getFileLocation());

        PutObjectRequest pr = PutObjectRequest.builder()
               .bucket(getBucket())
               .key(sb.toString())
               .build();

        Path filePath = Paths.get(file);
        s3Client.putObject(pr, filePath);
        return null;
    }

    @Override
    public void delete(String file) throws Exception {

    }

    public void setPath(String prefix) {
        StringBuilder sb  = new StringBuilder();

        sb.append(getPrefix()).append("/");
        sb.append(getName());
        sb.append(getFilePath());

        setFilePath(sb.toString());
    }

}
