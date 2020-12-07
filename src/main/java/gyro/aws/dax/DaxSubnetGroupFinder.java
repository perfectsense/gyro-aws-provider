/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeSubnetGroupsRequest;
import software.amazon.awssdk.services.dax.model.DescribeSubnetGroupsResponse;
import software.amazon.awssdk.services.dax.model.SubnetGroup;

/**
 * Query DAX subnet group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dax-subnet-group: $(external-query aws::dax-subnet-group { name: "subnet-group-example"})
 */
@Type("dax-subnet-group")
public class DaxSubnetGroupFinder extends AwsFinder<DaxClient, SubnetGroup, DaxSubnetGroupResource> {

    private String name;

    /**
     * The name of the subnet group.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<SubnetGroup> findAllAws(DaxClient client) {
        List<SubnetGroup> subnetGroups = new ArrayList<>();
        DescribeSubnetGroupsResponse response;
        String token = null;

        do {
            if (ObjectUtils.isBlank(token)) {
                response = client.describeSubnetGroups();
            } else {
                response = client.describeSubnetGroups(DescribeSubnetGroupsRequest.builder()
                    .nextToken(token)
                    .build());
            }

            if (response.hasSubnetGroups()) {
                subnetGroups.addAll(response.subnetGroups());
            }

            token = response.nextToken();
        } while (!ObjectUtils.isBlank(token));

        return subnetGroups;
    }

    @Override
    protected List<SubnetGroup> findAws(
        DaxClient client, Map<String, String> filters) {
        return client.describeSubnetGroups(r -> r.subnetGroupNames(filters.get("name"))).subnetGroups();
    }
}
