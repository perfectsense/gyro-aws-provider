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

package gyro.aws.elasticache;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheSubnetGroup;
import software.amazon.awssdk.services.elasticache.model.CacheSubnetGroupNotFoundException;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheSubnetGroupsResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a cache subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::elasticache-subnet-group cache-subnet-group-example
 *         name: "cache-subnet-group-example"
 *         description: "cache-subnet-group-desc"
 *         subnets: [
 *             $(aws::subnet subnet-cache-subnet-group-example-1),
 *             $(aws::subnet subnet-cache-subnet-group-example-2),
 *             $(aws::subnet subnet-cache-subnet-group-example-3)
 *         ]
 *     end
 */
@Type("elasticache-subnet-group")
public class CacheSubnetGroupResource extends AwsResource implements Copyable<CacheSubnetGroup> {
    private String name;
    private String description;
    private Set<SubnetResource> subnets;

    /**
     * The name of the cache subnet group. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the cache subnet group.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A list of subnets. (Required)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public void copyFrom(CacheSubnetGroup cacheSubnetGroup) {
        setName(cacheSubnetGroup.cacheSubnetGroupName());
        setDescription(cacheSubnetGroup.cacheSubnetGroupDescription());
        setSubnets(cacheSubnetGroup.subnets()
            .stream()
            .map(s -> findById(SubnetResource.class, s.subnetIdentifier()))
            .collect(Collectors.toSet()));
    }

    @Override
    public boolean refresh() {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        CacheSubnetGroup cacheSubnetGroup = getCacheSubnetGroup(client);

        if (cacheSubnetGroup == null) {
            return false;
        }

        copyFrom(cacheSubnetGroup);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.createCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getName())
                .cacheSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()))
        );

    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.modifyCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getName())
                .cacheSubnetGroupDescription(getDescription())
                .subnetIds(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElastiCacheClient client = createClient(ElastiCacheClient.class);

        client.deleteCacheSubnetGroup(
            r -> r.cacheSubnetGroupName(getName())
        );
    }

    private CacheSubnetGroup getCacheSubnetGroup(ElastiCacheClient client) {
        CacheSubnetGroup cacheSubnetGroup = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load cache subnet group.");
        }

        try {
            DescribeCacheSubnetGroupsResponse response = client.describeCacheSubnetGroups(
                r -> r.cacheSubnetGroupName(getName())
            );

            if (!response.cacheSubnetGroups().isEmpty()) {
                cacheSubnetGroup = response.cacheSubnetGroups().get(0);
            }

        } catch (CacheSubnetGroupNotFoundException ex) {
            // Ignore
        }

        return cacheSubnetGroup;
    }
}
