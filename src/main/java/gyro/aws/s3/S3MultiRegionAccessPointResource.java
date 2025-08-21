/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.CreateMultiRegionAccessPointRequest;
import software.amazon.awssdk.services.s3control.model.CreateMultiRegionAccessPointResponse;
import software.amazon.awssdk.services.s3control.model.DeleteMultiRegionAccessPointRequest;
import software.amazon.awssdk.services.s3control.model.DescribeMultiRegionAccessPointOperationRequest;
import software.amazon.awssdk.services.s3control.model.DescribeMultiRegionAccessPointOperationResponse;
import software.amazon.awssdk.services.s3control.model.GetMultiRegionAccessPointRequest;
import software.amazon.awssdk.services.s3control.model.GetMultiRegionAccessPointResponse;
import software.amazon.awssdk.services.s3control.model.MultiRegionAccessPointReport;
import software.amazon.awssdk.services.s3control.model.DeleteMultiRegionAccessPointResponse;
import software.amazon.awssdk.services.s3control.model.S3ControlException;
import software.amazon.awssdk.services.s3control.model.OperationName;
import software.amazon.awssdk.services.s3control.model.PublicAccessBlockConfiguration;
import software.amazon.awssdk.services.s3control.model.PutMultiRegionAccessPointPolicyRequest;
import software.amazon.awssdk.services.s3control.model.Region;
import software.amazon.awssdk.services.sts.StsClient;

/**
 * Creates an S3 Multi-Region Access Point that provides a global endpoint for S3 buckets across multiple regions.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::s3-multi-region-access-point mrap-example
 *         name: "example-mrap"
 *
 *         region
 *             bucket: $(aws::s3-bucket bucket-us-east-1)
 *         end
 *
 *         region
 *             bucket: $(aws::s3-bucket bucket-us-west-2)
 *         end
 *
 *         public-access-block-configuration
 *             block-public-acls: true
 *             ignore-public-acls: true
 *             block-public-policy: true
 *             restrict-public-buckets: true
 *         end
 *     end
 */
@Type("s3-multi-region-access-point")
public class S3MultiRegionAccessPointResource extends AwsResource implements Copyable<MultiRegionAccessPointReport> {

    private String name;
    private List<S3MultiRegionAccessPointRegion> regions;
    private S3PublicAccessBlockConfiguration publicAccessBlockConfiguration;
    
    // Read-only attributes
    private String arn;
    private String alias;
    private String domainName;
    private String status;

    /**
     * The name of the Multi-Region Access Point.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The regions and their associated buckets for this Multi-Region Access Point.
     *
     * @subresource gyro.aws.s3.S3MultiRegionAccessPointRegion
     */
    @Required
    public List<S3MultiRegionAccessPointRegion> getRegion() {
        if (regions == null) {
            regions = new ArrayList<>();
        }
        return regions;
    }

    public void setRegion(List<S3MultiRegionAccessPointRegion> regions) {
        this.regions = regions;
    }

    /**
     * The PublicAccessBlock configuration for the Multi-Region Access Point.
     *
     * @subresource gyro.aws.s3.S3PublicAccessBlockConfiguration
     */
    @Updatable
    public S3PublicAccessBlockConfiguration getPublicAccessBlockConfiguration() {
        return publicAccessBlockConfiguration;
    }

    public void setPublicAccessBlockConfiguration(S3PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
        this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
    }

    /**
     * The ARN of the Multi-Region Access Point.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The alias of the Multi-Region Access Point.
     */
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The domain name of the Multi-Region Access Point.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * The status of the Multi-Region Access Point.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void copyFrom(MultiRegionAccessPointReport mrap) {
        setName(mrap.name());
        // ARN is not available in MultiRegionAccessPointReport, it would come from GetMultiRegionAccessPoint
        setAlias(mrap.alias());
        setStatus(mrap.status().toString());
        
        if (mrap.alias() != null) {
            setDomainName(mrap.alias() + ".accesspoint.s3-global.amazonaws.com");
        }

        getRegion().clear();
        if (mrap.hasRegions()) {
            mrap.regions().forEach(regionReport -> {
                S3MultiRegionAccessPointRegion regionResource = newSubresource(S3MultiRegionAccessPointRegion.class);
                regionResource.copyFromRegionReport(regionReport);
                getRegion().add(regionResource);
            });
        }

        if (mrap.publicAccessBlock() != null) {
            S3PublicAccessBlockConfiguration blockConfig = newSubresource(S3PublicAccessBlockConfiguration.class);
            blockConfig.copyFromS3Control(mrap.publicAccessBlock());
            setPublicAccessBlockConfiguration(blockConfig);
        }
    }

    @Override
    public boolean refresh() {
        S3ControlClient client = createClient(S3ControlClient.class, "us-west-2", null);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load multi-region access point.");
        }

        try {
            GetMultiRegionAccessPointResponse response = client.getMultiRegionAccessPoint(
                GetMultiRegionAccessPointRequest.builder()
                    .accountId(getAccountId())
                    .name(getName())
                    .build()
            );

            copyFrom(response.accessPoint());
            return true;

        } catch (S3ControlException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        S3ControlClient client = createClient(S3ControlClient.class, "us-west-2", null);

        CreateMultiRegionAccessPointRequest.Builder requestBuilder = CreateMultiRegionAccessPointRequest.builder()
            .accountId(getAccountId())
            .clientToken(generateClientToken())
            .details(details -> details
                .name(getName())
                .regions(getRegion().stream()
                    .map(S3MultiRegionAccessPointRegion::toRegion)
                    .collect(Collectors.toList()))
                .publicAccessBlock(getPublicAccessBlockConfiguration() != null ? 
                    getPublicAccessBlockConfiguration().toS3ControlPublicAccessBlockConfiguration() : null)
            );

        CreateMultiRegionAccessPointResponse result = client.createMultiRegionAccessPoint(requestBuilder.build());
        
        waitForOperation(client, result.requestTokenARN(), TimeoutSettings.Action.CREATE);
        
        // Refresh to get the created resource details
        refresh();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        // Multi-Region Access Points have limited update operations
        // The main updatable field is the public access block configuration through a separate API
        if (changedFieldNames.contains("public-access-block-configuration")) {
            updatePublicAccessBlock();
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        S3ControlClient client = createClient(S3ControlClient.class, "us-west-2", null);

        DeleteMultiRegionAccessPointRequest request = DeleteMultiRegionAccessPointRequest.builder()
            .accountId(getAccountId())
            .clientToken(generateClientToken())
            .details(details -> details.name(getName()))
            .build();

        DeleteMultiRegionAccessPointResponse result = client.deleteMultiRegionAccessPoint(request);
        
        waitForOperation(client, result.requestTokenARN(), TimeoutSettings.Action.DELETE);
    }

    private String getAccountId() {
        StsClient stsClient = createClient(StsClient.class);
        return stsClient.getCallerIdentity().account();
    }

    private String generateClientToken() {
        return getName() + "-" + System.currentTimeMillis();
    }

    private void waitForOperation(S3ControlClient client, String requestTokenArn, TimeoutSettings.Action action) {
        boolean waitResult = Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> isOperationComplete(client, requestTokenArn));

        if (!waitResult) {
            throw new GyroException("Multi-Region Access Point operation did not complete within the timeout period - " + getName());
        }
    }

    private boolean isOperationComplete(S3ControlClient client, String requestTokenArn) {
        try {
            DescribeMultiRegionAccessPointOperationResponse response = client.describeMultiRegionAccessPointOperation(
                DescribeMultiRegionAccessPointOperationRequest.builder()
                    .accountId(getAccountId())
                    .requestTokenARN(requestTokenArn)
                    .build()
            );

            String status = response.asyncOperation().requestStatus();
            return "SUCCEEDED".equals(status) || "FAILED".equals(status);

        } catch (Exception ex) {
            return false;
        }
    }

    private void updatePublicAccessBlock() {
        // Note: Updating public access block configuration requires a separate API call
        // and is not currently implemented in this version
        throw new GyroException("Updating public access block configuration for Multi-Region Access Points is not yet supported");
    }
}