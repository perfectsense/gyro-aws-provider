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
 *    metric-alarm: $(aws::metric-alarm EXTERNAL/* | alarm-name = '')
 */
@Type("metric-alarm")
public class MetricAlarmFinder extends AwsFinder<CloudWatchClient, MetricAlarm, MetricAlarmResource> {
    private String alarmName;
    private String alarmNamePrefix;
    private String state;
    private String actionNamePrefix;

    /**
     * The name of the alarm. Cannot be specified if 'alarm-prefix' specified.
     */
    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    /**
     * The prefix of the alarm. Cannot be specified if 'alarm-name' specified.
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
        if (filters.containsKey("alarm-name") && filters.containsKey("alarm-prefix")) {
            throw new IllegalArgumentException("Either 'alarm-name' or 'alarm-prefix' can be specified, not both.");
        }

        return client.describeAlarmsPaginator(
            r -> r.alarmNames(filters.get("alarm-name"))
                .alarmNamePrefix(filters.get("alarm-prefix"))
                .actionPrefix(filters.get("action-prefix"))
                .stateValue(filters.get("state"))).metricAlarms().stream().collect(Collectors.toList());
    }
}
