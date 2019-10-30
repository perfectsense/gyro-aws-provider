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

package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.CreateSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.GetSizeConstraintSetResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraint;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a size constraint set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-size-constraint-set-regional size-constraint-set-example
 *         name: "size-constraint-set-example"
 *
 *         size-constraint
 *             field-to-match
 *                 type: "METHOD"
 *             end
 *             text-transformation: "NONE"
 *             comparison-operator: "EQ"
 *             size: 10
 *         end
 *     end
 */
@Type("waf-size-constraint-set-regional")
public class SizeConstraintSetResource extends gyro.aws.waf.common.SizeConstraintSetResource {
    private Set<SizeConstraintResource> sizeConstraint;

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.SizeConstraintResource
     */
    @Updatable
    public Set<SizeConstraintResource> getSizeConstraint() {
        if (sizeConstraint == null) {
            sizeConstraint = new HashSet<>();
        }
        return sizeConstraint;
    }

    public void setSizeConstraint(Set<SizeConstraintResource> sizeConstraint) {
        this.sizeConstraint = sizeConstraint;

        if (sizeConstraint.size() > 10) {
            throw new GyroException("Size constraint limit exception. Max 10 per Byte Match Set.");
        }
    }

    @Override
    public void copyFrom(SizeConstraintSet sizeConstraintSet) {
        setId(sizeConstraintSet.sizeConstraintSetId());
        setName(sizeConstraintSet.name());

        getSizeConstraint().clear();
        for (SizeConstraint sizeConstraint : sizeConstraintSet.sizeConstraints()) {
            SizeConstraintResource sizeConstraintResource = newSubresource(SizeConstraintResource.class);
            sizeConstraintResource.copyFrom(sizeConstraint);
            getSizeConstraint().add(sizeConstraintResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetSizeConstraintSetResponse response = getRegionalClient().getSizeConstraintSet(
            r -> r.sizeConstraintSetId(getId())
        );


        this.copyFrom(response.sizeConstraintSet());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        CreateSizeConstraintSetResponse response = client.createSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.sizeConstraintSet().sizeConstraintSetId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        client.deleteSizeConstraintSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sizeConstraintSetId(getId())
        );
    }

    SizeConstraintSet getSizeConstraintSet(WafRegionalClient client) {
        return client.getSizeConstraintSet(r -> r.sizeConstraintSetId(getId())).sizeConstraintSet();
    }
}
