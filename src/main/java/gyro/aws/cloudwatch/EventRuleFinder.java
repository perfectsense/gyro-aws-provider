package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query event rule.
 *
 * .. code-block:: gyro
 *
 *    event-rule: $(aws::event-rule EXTERNAL/* | rule-name = '')
 */
@Type("event-rule")
public class EventRuleFinder extends AwsFinder<CloudWatchEventsClient, Rule, EventRuleResource> {
    private String ruleName;

    /**
     * The name of the rule.
     */
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    protected List<Rule> findAllAws(CloudWatchEventsClient client) {
        List<Rule> rules = new ArrayList<>();
        String marker = null;
        ListRulesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRules();
            } else {
                response = client.listRules(ListRulesRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            rules.addAll(response.rules());
        } while (!ObjectUtils.isBlank(marker));

        return rules;
    }

    @Override
    protected List<Rule> findAws(CloudWatchEventsClient client, Map<String, String> filters) {
        return client.listRules(r -> r.namePrefix(filters.get("rule-name"))).rules();
    }
}
