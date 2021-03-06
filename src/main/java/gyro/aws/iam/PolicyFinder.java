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
import software.amazon.awssdk.services.iam.model.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query IAM policies.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    policy: $(external-query aws::iam-policy { arn: ''})
 */
@Type("iam-policy")
public class PolicyFinder extends AwsFinder<IamClient, Policy, PolicyResource> {

    private String arn;
    private String path;

    /**
     * The arn of the policy.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A prefix path to search for policies.
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
    protected List<Policy> findAws(IamClient client, Map<String, String> filters) {
        List<Policy> policy = new ArrayList<>();

        if (filters.containsKey("arn")) {
            policy.add(client.getPolicy(r -> r.policyArn(filters.get("arn"))).policy());
        }

        if (filters.containsKey("path")) {
            policy.addAll(client.listPoliciesPaginator(r -> r.pathPrefix(filters.get("path"))).policies().stream().collect(Collectors.toList()));
        }

        return policy;
    }

    @Override
    protected List<Policy> findAllAws(IamClient client) {
        return client.listPoliciesPaginator().policies().stream().collect(Collectors.toList());
    }
}
