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

package gyro.aws.iam;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.InstanceProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query instance profiles.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    instance-profile: $(external-query aws::iam-instance-profile { name: ''})
 */
@Type("iam-instance-profile")
public class InstanceProfileFinder extends AwsFinder<IamClient, InstanceProfile, InstanceProfileResource> {

    private String name;
    private String path;

    /**
     * The name of the instance profile.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A prefix path to search for instance profiles.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }

    @Override
    protected List<InstanceProfile> findAws(IamClient client, Map<String, String> filters) {
        List<InstanceProfile> instanceProfile = new ArrayList<>();

        if (filters.containsKey("name")) {
            instanceProfile.add(client.getInstanceProfile(r -> r.instanceProfileName(filters.get("name"))).instanceProfile());
        }

        if (filters.containsKey("path")) {
            instanceProfile.addAll(client.listInstanceProfilesPaginator(r -> r.pathPrefix(filters.get("path")))
                .instanceProfiles()
                .stream()
                .collect(Collectors.toList()));
        }

        return instanceProfile;
    }

    @Override
    protected List<InstanceProfile> findAllAws(IamClient client) {
        return client.listInstanceProfilesPaginator().instanceProfiles().stream().collect(Collectors.toList());
    }
}
