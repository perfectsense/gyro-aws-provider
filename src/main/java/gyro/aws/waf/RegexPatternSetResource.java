package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.BeamCore;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName("regex-pattern-set")
public class RegexPatternSetResource extends AwsResource {
    private String name;
    private String regexPatternSetId;
    private List<String> patterns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegexPatternSetId() {
        return regexPatternSetId;
    }

    public void setRegexPatternSetId(String regexPatternSetId) {
        this.regexPatternSetId = regexPatternSetId;
    }

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

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetRegexPatternSetResponse response = client.getRegexPatternSet(
            r -> r.regexPatternSetId(getRegexPatternSetId())
        );

        RegexPatternSet regexPatternSet = response.regexPatternSet();

        setName(regexPatternSet.name());
        setPatterns(new ArrayList<>(regexPatternSet.regexPatternStrings()));

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateRegexPatternSetResponse response = client.createRegexPatternSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        RegexPatternSet regexPatternSet = response.regexPatternSet();

        setRegexPatternSetId(regexPatternSet.regexPatternSetId());

        try {
            savePatterns(client, new ArrayList<>(), getPatterns());
        } catch (Exception ex) {
            BeamCore.ui().write("\n@|bold,blue Error saving patterns for Regex pattern match set - %s (%s)."
                + " Please retry to update the patterns|@", getName(), getRegexPatternSetId());
            ex.printStackTrace();
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        savePatterns(client, ((RegexPatternSetResource) current).getPatterns(), getPatterns());
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        boolean isReferenced = false;
        String referenceId = "";

        ListRegexMatchSetsResponse response = client.listRegexMatchSets();

        for (RegexMatchSetSummary regexMatchSetSummary : response.regexMatchSets()) {
            GetRegexMatchSetResponse regexMatchSetResponse = client.getRegexMatchSet(
                r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())
            );

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
                savePatterns(client, getPatterns(), new ArrayList<>());
            }

            GetRegexPatternSetResponse patternSetResponse = client.getRegexPatternSet(r -> r.regexPatternSetId(getRegexPatternSetId()));
            if (patternSetResponse.regexPatternSet().regexPatternStrings().isEmpty()) {
                client.deleteRegexPatternSet(
                    r -> r.changeToken(client.getChangeToken().changeToken())
                        .regexPatternSetId(getRegexPatternSetId())
                );
            } else {
                throw new BeamException(String.format("Cannot delete regex pattern set - %s, as it has patterns.",getRegexPatternSetId()));
            }
        } else {
            throw new BeamException(String.format("Cannot delete regex pattern set - %s, as it is referenced by regex match set - %s",getRegexPatternSetId(),referenceId));
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

        if (!regexPatternSetUpdates.isEmpty()) {
            client.updateRegexPatternSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .regexPatternSetId(getRegexPatternSetId())
                    .updates(regexPatternSetUpdates)
            );
        }
    }
}
