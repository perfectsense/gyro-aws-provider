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

package gyro.aws.cloudtrail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.ListTrailsRequest;
import software.amazon.awssdk.services.cloudtrail.model.Trail;
import software.amazon.awssdk.services.cloudtrail.model.TrailInfo;

/**
 * Query cloud trails.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    trail: $(external-query aws::trail { name: "example-trail" })
 */
@Type("trail")
public class TrailFinder extends AwsFinder<CloudTrailClient, Trail, TrailResource> {

    /**
     * The name of the trail.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Trail> findAllAws(CloudTrailClient client) {
        List<Trail> trails = new ArrayList<>();

        List<String> trailNames = client.listTrailsPaginator(ListTrailsRequest.builder()
                .build()).trails().stream().map(TrailInfo::name).collect(Collectors.toList());

        if (!trailNames.isEmpty()) {
            trails = client.describeTrails(t -> t.trailNameList(trailNames)).trailList();
        }
        return trails;
    }

    @Override
    protected List<Trail> findAws(CloudTrailClient client, Map<String, String> filters) {
        List<Trail> trails = new ArrayList<>();

        List<String> trailNames = client.listTrailsPaginator(ListTrailsRequest.builder()
                .build()).trails().stream().map(TrailInfo::name)
                .filter(t -> t.equals(filters.get("name"))).collect(Collectors.toList());

        if (!trailNames.isEmpty()) {
            trails = client.describeTrails(t -> t.trailNameList(trailNames)).trailList();
        }
        return trails;
    }
}
