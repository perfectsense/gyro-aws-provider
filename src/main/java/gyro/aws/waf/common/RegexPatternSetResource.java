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
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRegexPatternSetRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RegexPatternSetResource extends AbstractWafResource implements Copyable<RegexPatternSet> {
    private String name;
    private String regexPatternSetId;
    private Set<String> patterns;

    /**
     * The name of the regex pattern set.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    @Output
    public String getRegexPatternSetId() {
        return regexPatternSetId;
    }

    public void setRegexPatternSetId(String regexPatternSetId) {
        this.regexPatternSetId = regexPatternSetId;
    }

    /**
     * A list of regular expression patterns to filter request on.
     */
    @Required
    @Updatable
    public Set<String> getPatterns() {
        if (patterns == null) {
            patterns = new HashSet<>();
        }

        return patterns;
    }

    public void setPatterns(Set<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public void copyFrom(RegexPatternSet regexPatternSet) {
        setRegexPatternSetId(regexPatternSet.regexPatternSetId());
        setName(regexPatternSet.name());
        setPatterns(new HashSet<>(regexPatternSet.regexPatternStrings()));
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRegexPatternSetId())) {
            return false;
        }

        copyFrom(getRegexPatternSet());

        return true;
    }

    protected abstract void doCreate();

    @Override
    public void create(GyroUI ui, State state) {
        doCreate();

        try {
            savePatterns(new HashSet<>(), getPatterns());
        } catch (Exception ex) {
            ui.write("\n@|bold,blue Error saving patterns for Regex pattern match set - %s (%s)."
                + " Please retry to update the patterns|@", getName(), getRegexPatternSetId());
            ex.printStackTrace();
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        savePatterns(((RegexPatternSetResource) current).getPatterns(), getPatterns());
    }

    @Override
    public void delete(GyroUI ui, State state) {

        boolean isReferenced = false;
        String referenceId = "";

        for (RegexMatchSetSummary regexMatchSetSummary : getRegexMatchSetSummaries()) {

            isReferenced = getRegexMatchSet(regexMatchSetSummary.regexMatchSetId())
                .regexMatchTuples()
                .stream()
                .anyMatch(f -> f.regexPatternSetId().equals(getRegexPatternSetId()));

            if (isReferenced) {
                referenceId = getRegexPatternSetId();
                break;
            }
        }

        if (!isReferenced) {
            if (!getPatterns().isEmpty()) {
                savePatterns(getPatterns(), new HashSet<>());
            }

            RegexPatternSet regexPatternSet = getRegexPatternSet();

            if (regexPatternSet.regexPatternStrings().isEmpty()) {
                deleteRegexPatternSet();
            } else {
                throw new GyroException(String.format("Cannot delete regex pattern set - %s, as it has patterns.",getRegexPatternSetId()));
            }
        } else {
            throw new GyroException(String
                .format("Cannot delete regex pattern set - %s, as it is referenced by regex match set - %s",getRegexPatternSetId(),referenceId));
        }
    }

    protected abstract void savePatterns(Set<String> oldPatterns, Set<String> newPatterns);

    protected abstract void deleteRegexPatternSet();

    protected abstract List<RegexMatchSetSummary> getRegexMatchSetSummaries();

    protected abstract RegexMatchSet getRegexMatchSet(String regexMatchSetId);

    protected UpdateRegexPatternSetRequest.Builder toUpdateRegexPatternSetRequest(Set<String> oldPatterns, Set<String> newPatterns) {
        List<RegexPatternSetUpdate> regexPatternSetUpdates = new ArrayList<>();

        List<String> deletePatterns = oldPatterns.stream()
            .filter(f -> !newPatterns.contains(f))
            .collect(Collectors.toList());

        for (String pattern : deletePatterns) {
            regexPatternSetUpdates.add(
                RegexPatternSetUpdate.builder()
                    .action(ChangeAction.DELETE)
                    .regexPatternString(pattern)
                    .build()
            );
        }

        List<String> insertPatterns = newPatterns.stream()
            .filter(f -> !oldPatterns.contains(f))
            .collect(Collectors.toList());

        for (String pattern : insertPatterns) {
            regexPatternSetUpdates.add(
                RegexPatternSetUpdate.builder()
                    .action(ChangeAction.INSERT)
                    .regexPatternString(pattern)
                    .build()
            );
        }

        return UpdateRegexPatternSetRequest.builder()
            .regexPatternSetId(getRegexPatternSetId())
            .updates(regexPatternSetUpdates);
    }

    protected abstract RegexPatternSet getRegexPatternSet();
}
