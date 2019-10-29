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

package gyro.aws.dlm;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dlm.DlmClient;
import software.amazon.awssdk.services.dlm.model.LifecyclePolicy;
import software.amazon.awssdk.services.dlm.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query ebs snapshot lifecycle policy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    lifecycle-policy: $(external-query aws::dlm-lifecycle-policy { id: 'policy-xxxxxxxxxxx' })
 */
@Type("dlm-lifecycle-policy")
public class LifecyclePolicyFinder extends AwsFinder<DlmClient, LifecyclePolicy, LifecyclePolicyResource> {
    private String id;

    /**
     * The policy ID.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<LifecyclePolicy> findAllAws(DlmClient client) {
        List<LifecyclePolicy> lifecyclePolicies = new ArrayList<>();

        client.getLifecyclePolicies().policies().forEach(o -> lifecyclePolicies.add(client.getLifecyclePolicy(r -> r.policyId(o.policyId())).policy()));

        return lifecyclePolicies;
    }

    @Override
    protected List<LifecyclePolicy> findAws(DlmClient client, Map<String, String> filters) {
        List<LifecyclePolicy> lifecyclePolicies = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                lifecyclePolicies.add(client.getLifecyclePolicy(r -> r.policyId(filters.get("id"))).policy());
            } catch (ResourceNotFoundException ignore) {
                // ignore
            }
        }

        return lifecyclePolicies;
    }
}
