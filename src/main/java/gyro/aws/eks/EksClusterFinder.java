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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;

/**
 * Query eks cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cluster: $(external-query aws::eks-cluster { name: 'example-eks-cluster' })
 */
@Type("eks-cluster")
public class EksClusterFinder extends AwsFinder<EksClient, Cluster, EksClusterResource> {

    private String name;

    /**
     * The name of the EKS cluster.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Cluster> findAllAws(EksClient client) {
        return client.listClustersPaginator()
            .clusters()
            .stream()
            .map(s -> client.describeCluster(DescribeClusterRequest.builder().name(s).build()).cluster())
            .collect(Collectors.toList());
    }

    @Override
    protected List<Cluster> findAws(EksClient client, Map<String, String> filters) {
        return client.listClustersPaginator()
            .clusters()
            .stream()
            .filter(s -> s.equals(getName()))
            .map(s -> client.describeCluster(DescribeClusterRequest.builder().name(s).build()).cluster())
            .collect(Collectors.toList());
    }
}
