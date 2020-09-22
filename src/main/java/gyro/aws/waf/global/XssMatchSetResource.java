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

package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetXssMatchSetResponse;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a global xss match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-xss-match-set xss-match-set-example
 *         name: "xss-match-set-example"
 *
 *         xss-match-tuple
 *             field-to-match
 *                 type: "METHOD"
 *             end
 *             text-transformation: "NONE"
 *         end
 *     end
 */
@Type("waf-xss-match-set")
public class XssMatchSetResource extends gyro.aws.waf.common.XssMatchSetResource {
    private Set<XssMatchTupleResource> xssMatchTuple;

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.XssMatchTupleResource
     */
    @Required
    @Updatable
    public Set<XssMatchTupleResource> getXssMatchTuple() {
        if (xssMatchTuple == null) {
            xssMatchTuple = new HashSet<>();
        }

        return xssMatchTuple;
    }

    public void setXssMatchTuple(Set<XssMatchTupleResource> xssMatchTuple) {
        this.xssMatchTuple = xssMatchTuple;

        if (xssMatchTuple.size() > 10) {
            throw new GyroException("Xss Match Tuple limit exception. Max 10 per Byte Match Set.");
        }
    }

    @Override
    public void copyFrom(XssMatchSet xssMatchSet) {
        setId(xssMatchSet.xssMatchSetId());
        setName(xssMatchSet.name());

        getXssMatchTuple().clear();
        for (XssMatchTuple xssMatchTuple : xssMatchSet.xssMatchTuples()) {
            XssMatchTupleResource xssMatchTupleResource = newSubresource(XssMatchTupleResource.class);
            xssMatchTupleResource.copyFrom(xssMatchTuple);
            getXssMatchTuple().add(xssMatchTupleResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetXssMatchSetResponse response = getGlobalClient().getXssMatchSet(
                r -> r.xssMatchSetId(getId())
            );

        this.copyFrom(response.xssMatchSet());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        CreateXssMatchSetResponse response = client.createXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.xssMatchSet().xssMatchSetId());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafClient client = getGlobalClient();

        client.deleteXssMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .xssMatchSetId(getId())
        );
    }

    XssMatchSet getXssMatchSet(WafClient client) {
        return client.getXssMatchSet(r -> r.xssMatchSetId(getId())).xssMatchSet();
    }
}
