package gyro.aws.waf;

import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRegexPatternSetRequest;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a regex pattern set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::regex-pattern-set regex-pattern-set-example
 *         name: "regex-pattern-set-example"
 *
 *         patterns: [
 *             "pattern1",
 *             "pattern2"
 *         ]
 *     end
 */
@ResourceName("regex-pattern-set")
public class RegexPatternSetResource extends AbstractWafResource {
    private String name;
    private String regexPatternSetId;
    private List<String> patterns;

    /**
     * The name of the regex pattern set. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getRegexPatternSetId() {
        return regexPatternSetId;
    }

    public void setRegexPatternSetId(String regexPatternSetId) {
        this.regexPatternSetId = regexPatternSetId;
    }

    /**
     * A list of regular expression patterns to filter request on. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getPatterns() {
        if (patterns == null) {
            patterns = new ArrayList<>();
        }

        Collections.sort(patterns);

        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRegexPatternSetId())) {
            return false;
        }

        RegexPatternSet regexPatternSet = getRegexPatternSet();

        setName(regexPatternSet.name());
        setPatterns(new ArrayList<>(regexPatternSet.regexPatternStrings()));

        return true;
    }

    @Override
    public void create() {
        CreateRegexPatternSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createRegexPatternSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createRegexPatternSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        RegexPatternSet regexPatternSet = response.regexPatternSet();

        setRegexPatternSetId(regexPatternSet.regexPatternSetId());

        try {
            if (getRegionalWaf()) {
                savePatterns(getRegionalClient(), new ArrayList<>(), getPatterns());
            } else {
                savePatterns(getGlobalClient(), new ArrayList<>(), getPatterns());
            }
        } catch (Exception ex) {
            GyroCore.ui().write("\n@|bold,blue Error saving patterns for Regex pattern match set - %s (%s)."
                + " Please retry to update the patterns|@", getName(), getRegexPatternSetId());
            ex.printStackTrace();
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        if (getRegionalWaf()) {
            savePatterns(getRegionalClient(), ((RegexPatternSetResource) current).getPatterns(), getPatterns());
        } else {
            savePatterns(getGlobalClient(), ((RegexPatternSetResource) current).getPatterns(), getPatterns());
        }
    }

    @Override
    public void delete() {

        boolean isReferenced = false;
        String referenceId = "";

        ListRegexMatchSetsResponse response = getRegionalWaf() ? getRegionalClient().listRegexMatchSets() : getGlobalClient().listRegexMatchSets();

        for (RegexMatchSetSummary regexMatchSetSummary : response.regexMatchSets()) {
            GetRegexMatchSetResponse regexMatchSetResponse;

            if (getRegionalWaf()) {
                regexMatchSetResponse = getRegionalClient().getRegexMatchSet(
                    r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())
                );
            } else {
                regexMatchSetResponse = getGlobalClient().getRegexMatchSet(
                    r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())
                );
            }

            isReferenced = regexMatchSetResponse
                .regexMatchSet()
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
                if (getRegionalWaf()) {
                    savePatterns(getRegionalClient(), getPatterns(), new ArrayList<>());
                } else {
                    savePatterns(getGlobalClient(), getPatterns(), new ArrayList<>());
                }
            }

            RegexPatternSet regexPatternSet = getRegexPatternSet();

            if (regexPatternSet.regexPatternStrings().isEmpty()) {
                if (getRegionalWaf()) {
                    WafRegionalClient client = getRegionalClient();

                    client.deleteRegexPatternSet(
                        r -> r.changeToken(client.getChangeToken().changeToken())
                            .regexPatternSetId(getRegexPatternSetId())
                    );
                } else {
                    WafClient client = getGlobalClient();

                    client.deleteRegexPatternSet(
                        r -> r.changeToken(client.getChangeToken().changeToken())
                            .regexPatternSetId(getRegexPatternSetId())
                    );
                }
            } else {
                throw new GyroException(String.format("Cannot delete regex pattern set - %s, as it has patterns.",getRegexPatternSetId()));
            }
        } else {
            throw new GyroException(String
                .format("Cannot delete regex pattern set - %s, as it is referenced by regex match set - %s",getRegexPatternSetId(),referenceId));
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("regex pattern set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getRegexPatternSetId())) {
            sb.append(" - ").append(getRegexPatternSetId());
        }

        return sb.toString();
    }

    private void savePatterns(WafClient client, List<String> oldPatterns, List<String> newPatterns) {
        UpdateRegexPatternSetRequest regexPatternSetRequest = getUpdateRegexPatternSetRequest(oldPatterns, newPatterns)
            .changeToken(client.getChangeToken().changeToken())
            .build();


        if (!regexPatternSetRequest.updates().isEmpty()) {
            client.updateRegexPatternSet(regexPatternSetRequest);
        }
    }

    private void savePatterns(WafRegionalClient client, List<String> oldPatterns, List<String> newPatterns) {
        UpdateRegexPatternSetRequest regexPatternSetRequest = getUpdateRegexPatternSetRequest(oldPatterns, newPatterns)
            .changeToken(client.getChangeToken().changeToken())
            .build();


        if (!regexPatternSetRequest.updates().isEmpty()) {
            client.updateRegexPatternSet(regexPatternSetRequest);
        }
    }


    private UpdateRegexPatternSetRequest.Builder getUpdateRegexPatternSetRequest(List<String> oldPatterns, List<String> newPatterns) {
        List<RegexPatternSetUpdate> regexPatternSetUpdates = new ArrayList<>();

        List<String> deletePatterns = oldPatterns.stream()
            .filter(f -> !new HashSet<>(newPatterns).contains(f))
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
            .filter(f -> !new HashSet<>(oldPatterns).contains(f))
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

    private RegexPatternSet getRegexPatternSet() {
        GetRegexPatternSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getRegexPatternSet(
                r -> r.regexPatternSetId(getRegexPatternSetId())
            );
        } else {
            response = getGlobalClient().getRegexPatternSet(
                r -> r.regexPatternSetId(getRegexPatternSetId())
            );
        }

        return response.regexPatternSet();
    }
}
