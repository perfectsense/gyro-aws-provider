package gyro.aws.cloudwatch;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query metric alarm.
 *
 * .. code-block:: gyro
 *
 *    metric-alarm: $(aws::cloudwatch-metric-alarm EXTERNAL/* | name = '')
 */
@Type("cloudwatch-metric-alarm")
public class MetricAlarmFinder extends AwsFinder<CloudWatchClient, MetricAlarm, MetricAlarmResource> {
    private String name;
    private String alarmNamePrefix;
    private String state;
    private String actionNamePrefix;

    /**
     * The name of the alarm. Cannot be specified if 'alarm-prefix' specified.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The prefix of the alarm. Cannot be specified if 'name' specified.
     */
    public String getAlarmNamePrefix() {
        return alarmNamePrefix;
    }

    public void setAlarmNamePrefix(String alarmNamePrefix) {
        this.alarmNamePrefix = alarmNamePrefix;
    }

    /**
     * The state of the alarm.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The action prefix of the alarm.
     */
    public String getActionNamePrefix() {
        return actionNamePrefix;
    }

    public void setActionNamePrefix(String actionNamePrefix) {
        this.actionNamePrefix = actionNamePrefix;
    }

    @Override
    protected List<MetricAlarm> findAllAws(CloudWatchClient client) {
        return client.describeAlarmsPaginator().metricAlarms().stream().collect(Collectors.toList());
    }

    @Override
    protected List<MetricAlarm> findAws(CloudWatchClient client, Map<String, String> filters) {
        if (filters.containsKey("name") && filters.containsKey("alarm-prefix")) {
            throw new IllegalArgumentException("Either 'name' or 'alarm-prefix' can be specified, not both.");
        }

        return client.describeAlarmsPaginator(
            r -> r.alarmNames(filters.get("name"))
                .alarmNamePrefix(filters.get("alarm-prefix"))
                .actionPrefix(filters.get("action-prefix"))
                .stateValue(filters.get("state"))).metricAlarms().stream().collect(Collectors.toList());
    }
}
