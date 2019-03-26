package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.PublishLayerVersionRequest;
import software.amazon.awssdk.services.lambda.model.PublishLayerVersionResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a lambda layer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-layer lambda-layer-example
 *         layer-name: "lambda-layer-example"
 *         description: "lambda-layer-example-desc"
 *         content-zip-path: "example-function.zip"
 *         compatible-runtimes: [
 *             "nodejs8.10"
 *         ]
 *     end
 */
@ResourceName("lambda-layer")
public class LayerResource extends AwsResource {
    private String layerName;
    private String description;
    private String licenseInfo;
    private List<String> compatibleRuntimes;
    private String s3Bucket;
    private String s3Key;
    private String s3ObjectVersion;
    private String contentZipPath;

    // -- Readonly

    private String arn;
    private String versionArn;
    private Long version;
    private String createdDate;

    /**
     * The name of the layer. (Required)
     */
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    /**
     * The description of the layer.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The software license for the layer.
     */
    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    /**
     * The list of runtime language for the function using this layer. Valid values are ``nodejs`` or ``nodejs4.3`` or ``nodejs6.10`` or ``nodejs8.10`` or ``java8`` or ``python2.7`` or ``python3.6`` or ``python3.7`` or ``dotnetcore1.0`` or ``dotnetcore2.0`` or ``dotnetcore2.1`` or ``nodejs4.3-edge`` or ``go1.x`` or ``ruby2.5`` or ``provided``. (Required)
     */
    public List<String> getCompatibleRuntimes() {
        return compatibleRuntimes;
    }

    public void setCompatibleRuntimes(List<String> compatibleRuntimes) {
        if (compatibleRuntimes == null) {
            compatibleRuntimes = new ArrayList<>();
        }

        this.compatibleRuntimes = compatibleRuntimes;
    }

    /**
     * The s3 bucket name where the code for the function using this layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * The s3 object key where the code for the function using this layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    /**
     * The s3 object version where the code for the function using this layer resides. Required if field 'content-zip-path' not set.
     */
    public String getS3ObjectVersion() {
        return s3ObjectVersion;
    }

    public void setS3ObjectVersion(String s3ObjectVersion) {
        this.s3ObjectVersion = s3ObjectVersion;
    }

    /**
     * The zip file location where the code for the function using this layer resides. Required if fields 's3-bucket', 's3-key' and 's3-object-version' not set.
     */
    public String getContentZipPath() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;
    }

    /**
     * The ARN of the lambda layer.
     */
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ARN of the lambda layer version specific.
     */
    @ResourceOutput
    public String getVersionArn() {
        return versionArn;
    }

    public void setVersionArn(String versionArn) {
        this.versionArn = versionArn;
    }

    /**
     * The version of the lambda layer.
     */
    @ResourceOutput
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * The date that the layer version was created.
     */
    @ResourceOutput
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getLayerName()) || getVersion() == null) {
            throw new BeamException("layer-name and/or version is missing, unable to load lambda layer.");
        }

        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetLayerVersionResponse response = client.getLayerVersion(
                r -> r.layerName(getLayerName())
                    .versionNumber(getVersion())
            );

            setCompatibleRuntimes(response.compatibleRuntimesAsStrings());
            setCreatedDate(response.createdDate());
            setDescription(response.description());
            setArn(response.layerArn());
            setVersionArn(response.layerVersionArn());
            setLicenseInfo(response.licenseInfo());
        } catch (ResourceNotFoundException ex) {
            return false;
        }


        return true;
    }

    @Override
    public void create() {
        LambdaClient client = createClient(LambdaClient.class);

        PublishLayerVersionRequest.Builder builder = PublishLayerVersionRequest.builder()
            .compatibleRuntimes(getCompatibleRuntimes().stream().map(Runtime::fromValue).collect(Collectors.toList()))
            .layerName(getLayerName())
            .description(getDescription())
            .licenseInfo(getLicenseInfo());

        if (!ObjectUtils.isBlank(getContentZipPath())) {
            builder = builder.content(c -> c.zipFile(getZipFile()));

        } else {
            builder = builder.content(c -> c.s3Bucket(getS3Bucket())
                .s3Key(getS3Key())
                .s3ObjectVersion(getS3ObjectVersion())
            );
        }

        PublishLayerVersionResponse response = client.publishLayerVersion(builder.build());

        setArn(response.layerArn());
        setVersionArn(response.layerVersionArn());
        setVersion(response.version());
        setCreatedDate(response.createdDate());
    }

    @Override
    public void update(Resource resource, Set<String> set) {

    }

    @Override
    public void delete() {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteLayerVersion(
            r -> r.layerName(getLayerName())
                .versionNumber(getVersion())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("lambda layer");

        if (!ObjectUtils.isBlank(getLayerName())) {
            sb.append(" - ").append(getLayerName());
        }

        if (!ObjectUtils.isBlank(getVersion())) {
            sb.append(" version - ").append(getVersion());
        }

        return sb.toString();
    }

    private SdkBytes getZipFile() {
        try {
            String dir = scope().getFileScope().getFile().substring(0, scope().getFileScope().getFile().lastIndexOf(File.separator));
            return SdkBytes.fromByteArray(Files.readAllBytes(Paths.get(dir + File.separator + getContentZipPath())));
        } catch (IOException ex) {
            throw new BeamException(String.format("File not found - %s",getContentZipPath()));
        }
    }
}
