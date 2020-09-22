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

package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.diff.Update;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     default-action
 *        target-group: $(aws::load-balancer-target-group target-group-example)
 *        type: "forward"
 *     end
 */
public class NetworkActionResource extends AwsResource {

    private TargetGroupResource targetGroup;
    private String type;

    /**
     *  The target group that this action is associated with  (Optional)
     */
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     *  The type of action to perform  (Required)
     */
    @Required
    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Create) {
            return;
        }

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        if (DiffableInternals.getChange(parentResource()) instanceof Update) {
            return;
        }

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void delete(GyroUI ui, State state) {}

    public Action toAction() {
        return Action.builder()
                .targetGroupArn(getTargetGroup().getArn())
                .type(getType())
                .build();
    }
}
