/*
 * Copyright 2020, Perfect Sense, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.DescribeFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.FargateProfile;
import software.amazon.awssdk.services.eks.model.ListFargateProfilesRequest;

/**
 * Query eks fargate profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    profile: $(external-query aws::eks-fargate-profile { name: 'example-eks-fargate-profile' })
 */
@Type("eks-fargate-profile")
public class EksFargateProfileFinder extends AwsFinder<EksClient, FargateProfile, EksFargateProfileResource> {

    private String clusterName;
    private String name;

    /**
     * The name of the cluster that has the fargate profile.
     */
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * The name of the fargate profile.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<FargateProfile> findAllAws(EksClient client) {
        List<FargateProfile> profiles = new ArrayList<>();

        client.listClustersPaginator()
            .clusters()
            .stream()
            .forEach(c -> profiles.addAll(client.listFargateProfiles(ListFargateProfilesRequest.builder()
                .clusterName(c)
                .build())
                .fargateProfileNames()
                .stream()
                .map(f -> client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .clusterName(c)
                    .fargateProfileName(f)
                    .build()).fargateProfile())
                .collect(Collectors.toList())));

        return profiles;
    }

    @Override
    protected List<FargateProfile> findAws(EksClient client, Map<String, String> filters) {
        List<FargateProfile> profiles = new ArrayList<>();

        if (filters.containsKey("cluster-name") && filters.containsKey("name")) {
            profiles.add(client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                .fargateProfileName(filters.get("name"))
                .clusterName(filters.get("cluster-name"))
                .build()).fargateProfile());

        } else if (filters.containsKey("cluster-name")) {
            profiles.addAll(client.listFargateProfiles(ListFargateProfilesRequest.builder()
                .clusterName(filters.get("cluster-name"))
                .build())
                .fargateProfileNames()
                .stream()
                .map(f -> client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .clusterName(filters.get("cluster-name"))
                    .fargateProfileName(f)
                    .build()).fargateProfile())
                .collect(Collectors.toList()));

        } else {
            client.listClustersPaginator()
                .clusters()
                .stream()
                .filter(c -> client.listFargateProfiles(ListFargateProfilesRequest.builder().clusterName(c).build())
                    .fargateProfileNames()
                    .contains(filters.get("name")))
                .findFirst()
                .ifPresent(cluster -> profiles.add(client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .fargateProfileName(filters.get("name"))
                    .clusterName(cluster)
                    .build()).fargateProfile()));

        }

        return profiles;
    }
}
