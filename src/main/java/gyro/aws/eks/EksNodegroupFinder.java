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
import software.amazon.awssdk.services.eks.model.DescribeNodegroupRequest;
import software.amazon.awssdk.services.eks.model.ListNodegroupsRequest;
import software.amazon.awssdk.services.eks.model.Nodegroup;

/**
 * Query eks nodegroup.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nodegroup: $(external-query aws::eks-nodegroup { name: 'example-nodegroup' })
 */
@Type("eks-nodegroup")
public class EksNodegroupFinder extends AwsFinder<EksClient, Nodegroup, EksNodegroupResource> {

    private String clusterName;
    private String name;

    /**
     * The name of the cluster that has the nodegroup.
     */
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * The name of the nodegroup.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Nodegroup> findAllAws(EksClient client) {
        List<Nodegroup> profiles = new ArrayList<>();

        client.listClustersPaginator()
            .clusters()
            .stream()
            .forEach(c -> profiles.addAll(client.listNodegroups(ListNodegroupsRequest.builder()
                .clusterName(c)
                .build())
                .nodegroups()
                .stream()
                .map(f -> client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .clusterName(c)
                    .nodegroupName(f)
                    .build()).nodegroup())
                .collect(Collectors.toList())));

        return profiles;
    }

    @Override
    protected List<Nodegroup> findAws(EksClient client, Map<String, String> filters) {
        List<Nodegroup> profiles = new ArrayList<>();

        if (filters.containsKey("cluster-name") && filters.containsKey("name")) {
            profiles.add(client.describeNodegroup(DescribeNodegroupRequest.builder()
                .nodegroupName(filters.get("name"))
                .clusterName(filters.get("cluster-name"))
                .build()).nodegroup());

        } else if (filters.containsKey("cluster-name")) {
            profiles.addAll(client.listNodegroups(ListNodegroupsRequest.builder()
                .clusterName(filters.get("cluster-name"))
                .build())
                .nodegroups()
                .stream()
                .map(f -> client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .clusterName(filters.get("cluster-name"))
                    .nodegroupName(f)
                    .build()).nodegroup())
                .collect(Collectors.toList()));

        } else {
            client.listClustersPaginator()
                .clusters()
                .stream()
                .filter(c -> client.listNodegroups(ListNodegroupsRequest.builder().clusterName(c).build())
                    .nodegroups()
                    .contains(filters.get("name")))
                .findFirst()
                .ifPresent(cluster -> profiles.add(client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .nodegroupName(filters.get("name"))
                    .clusterName(cluster)
                    .build()).nodegroup()));

        }

        return profiles;
    }
}
