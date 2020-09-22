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

package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.UpdateXssMatchSetRequest;
import software.amazon.awssdk.services.waf.model.XssMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;

import java.util.Set;

public abstract class XssMatchTupleResource extends AbstractWafResource implements Copyable<XssMatchTuple> {
    private FieldToMatch fieldToMatch;
    private String textTransformation;

    /**
     * The field setting to match the condition. (Required)
     */
    @Required
    public FieldToMatch getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatch fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    /**
     * Text transformation on the data provided before doing the check. Valid values are ``NONE`` or ``COMPRESS_WHITE_SPACE`` or ``HTML_ENTITY_DECODE`` or ``LOWERCASE`` or ``CMD_LINE`` or ``URL_DECODE``. (Required)
     */
    @Required
    public String getTextTransformation() {
        return textTransformation != null ? textTransformation.toUpperCase() : null;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

    @Override
    public void copyFrom(XssMatchTuple xssMatchTuple) {
        setTextTransformation(xssMatchTuple.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(xssMatchTuple.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        saveXssMatchTuple(false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        saveXssMatchTuple(true);
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getTextTransformation());

        if (getFieldToMatch() != null) {
            if (!ObjectUtils.isBlank(getFieldToMatch().getData())) {
                sb.append(" ").append(getFieldToMatch().getData());
            }

            if (!ObjectUtils.isBlank(getFieldToMatch().getType())) {
                sb.append(" ").append(getFieldToMatch().getType());
            }
        }

        return sb.toString();
    }

    protected abstract void saveXssMatchTuple(boolean isDelete);

    private XssMatchTuple toXssMatchTuple() {
        return XssMatchTuple.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .textTransformation(getTextTransformation())
            .build();
    }

    protected UpdateXssMatchSetRequest.Builder toUpdateXssMatchSetRequest(boolean isDelete) {
        XssMatchSetResource parent = (XssMatchSetResource) parent();

        XssMatchSetUpdate xssMatchSetUpdate = XssMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .xssMatchTuple(toXssMatchTuple())
            .build();

        return UpdateXssMatchSetRequest.builder()
            .xssMatchSetId(parent.getId())
            .updates(xssMatchSetUpdate);
    }
}
