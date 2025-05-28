/*
 * Copyright 2021, Perfect Sense, Inc.
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

package gyro.aws.eks;

import gyro.aws.AwsCredentials;
import gyro.core.Type;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.DiffableType;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Addon;
import software.amazon.awssdk.services.eks.model.DescribeAddonResponse;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

/**
 * Creates an eks addon.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-addon example-addon
 *         addon-name: vpc-cni
 *         cluster: $(aws::eks-cluster ex)
 *
 *         tags : {
 *             Name: "example-addon"
 *         }
 *     end
 */
@Type("eks-addon")
public class EksStandaloneAddonResource extends EksAddonResource {

    private EksClusterResource cluster;

    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    @Override
    public void copyFrom(Addon model) {
        setCluster(findById(EksClusterResource.class,
            EksClusterResource.getArnFromName(getRegion(), getOwnerId(), model.clusterName())));
        super.copyFrom(model);
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        try {
            DescribeAddonResponse response = client.describeAddon(r -> r
                .clusterName(clusterName())
                .addonName(getAddonName()));

            copyFrom(response.addon());
        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public String primaryKey() {
        String name = DiffableInternals.getName(this);
        return String.format("%s::%s", DiffableType.getInstance(getClass()).getName(), name);
    }

    @Override
    protected String clusterName() {
        return cluster.getName();
    }

    public String getRegion() {
        AwsCredentials credentials = credentials(AwsCredentials.class);
        return credentials.getRegion();
    }

    public String getOwnerId() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }
}
