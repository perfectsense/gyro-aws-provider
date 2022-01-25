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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Addon;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;

/**
 * Query eks addon.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    eks-addon: $(external-query aws::eks-addon { name: 'vpc-cni' })
 */
@Type("eks-addon")
public class EksStandaloneAddonFinder extends AwsFinder<EksClient, Addon, EksStandaloneAddonResource> {

    private String name;
    private String clusterName;

    /**
     * The name of the addon.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the cluster that the addon belongs to.
     */
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    protected List<Addon> findAllAws(EksClient client) {
        List<Addon> addons = new ArrayList<>();
        List<String> clusters = client.listClusters().clusters();

        clusters.forEach(c -> addons.addAll(client.listAddons(r -> r.clusterName(c)).addons().stream()
            .map(a -> client.describeAddon(d -> d.clusterName(c).addonName(a)).addon())
            .collect(Collectors.toList())));

        return addons;
    }

    @Override
    protected List<Addon> findAws(EksClient client, Map<String, String> filters) {
        List<Addon> addons = new ArrayList<>();
        List<String> clusters = new ArrayList<>();

        if (!filters.containsKey("cluster-name")) {
            clusters = client.listClusters().clusters();

        } else {
            clusters.add(filters.get("cluster-name"));
        }

        clusters.forEach(c -> {
            try {
                if (filters.containsKey("name")) {
                    addons.add(client.describeAddon(r -> r.addonName(filters.get("name")).clusterName(c)).addon());

                } else {
                    addons.addAll(client.listAddons(r -> r.clusterName(c)).addons().stream()
                        .map(a -> client.describeAddon(d -> d.clusterName(c).addonName(a)).addon())
                        .collect(Collectors.toList()));
                }
            } catch (ResourceNotFoundException ex) {
                // ignore
            }
        });

        return addons;
    }
}
