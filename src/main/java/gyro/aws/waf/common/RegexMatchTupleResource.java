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
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.RegexMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.model.UpdateRegexMatchSetRequest;

import java.util.Set;

public abstract class RegexMatchTupleResource extends AbstractWafResource implements Copyable<RegexMatchTuple> {
    private FieldToMatch fieldToMatch;
    private String textTransformation;
    private CommonRegexPatternSet regexPatternSet;

    /**
     * The field setting to match the condition. (Required)
     */
    public FieldToMatch getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatch fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    /**
     * Text transformation on the data provided before doing the check. Valid values are ``NONE`` or ``COMPRESS_WHITE_SPACE`` or ``HTML_ENTITY_DECODE`` or ``LOWERCASE`` or ``CMD_LINE`` or ``URL_DECODE``. (Required)
     */
    public String getTextTransformation() {
        return textTransformation != null ? textTransformation.toUpperCase() : null;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

    /**
     * The regex pattern set, having the regex patterns to filter the request.
     */
    public CommonRegexPatternSet getRegexPatternSet() {
        return regexPatternSet;
    }

    public void setRegexPatternSet(CommonRegexPatternSet regexPatternSet) {
        this.regexPatternSet = regexPatternSet;
    }

    @Override
    public void copyFrom(RegexMatchTuple regexMatchTuple) {
        setRegexPatternSet(findById(CommonRegexPatternSet.class, regexMatchTuple.regexPatternSetId()));
        setTextTransformation(regexMatchTuple.textTransformationAsString());

        FieldToMatch fieldToMatch = newSubresource(FieldToMatch.class);
        fieldToMatch.copyFrom(regexMatchTuple.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        saveRegexMatchTuple(toRegexMatchTuple(), false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        saveRegexMatchTuple(toRegexMatchTuple(), true);
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder();

        sb.append(getTextTransformation());

        if (getRegexPatternSet() != null && !ObjectUtils.isBlank(getRegexPatternSet().getRegexPatternSetId())) {
            sb.append(getRegexPatternSet().getRegexPatternSetId());
        }

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

    private RegexMatchTuple toRegexMatchTuple() {
        return RegexMatchTuple.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .regexPatternSetId(getRegexPatternSet().getRegexPatternSetId())
            .textTransformation(getTextTransformation())
            .build();
    }

    protected abstract void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete);

    protected UpdateRegexMatchSetRequest.Builder toUpdateRegexMatchSetRequest(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        RegexMatchSetResource parent = (RegexMatchSetResource) parent();

        RegexMatchSetUpdate regexMatchSetUpdate = RegexMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .regexMatchTuple(regexMatchTuple)
            .build();

        return UpdateRegexMatchSetRequest.builder()
            .regexMatchSetId(parent.getId())
            .updates(regexMatchSetUpdate);
    }
}
