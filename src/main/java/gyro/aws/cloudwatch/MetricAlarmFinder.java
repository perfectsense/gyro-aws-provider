package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        List<MetricAlarm> metricAlarms = new ArrayList<>();

        String marker = null;
        DescribeAlarmsResponse response;
        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeAlarms();
            } else {
                response = client.describeAlarms(DescribeAlarmsRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            metricAlarms.addAll(response.metricAlarms());
        } while (!ObjectUtils.isBlank(marker));

        return metricAlarms;
    }

    @Override
    protected List<MetricAlarm> findAws(CloudWatchClient client, Map<String, String> filters) {
        List<MetricAlarm> metricAlarms = new ArrayList<>();

        DescribeAlarmsRequest.Builder builder = DescribeAlarmsRequest.builder();

        if (filters.containsKey("alarm-name") && filters.containsKey("alarm-prefix")) {
            throw new IllegalArgumentException("Either 'alarm-name' or 'alarm-prefix' can be specified, not both.");
        }

        if (filters.containsKey("alarm-name") && !ObjectUtils.isBlank(filters.get("alarm-name"))) {
            builder = builder.alarmNames(Collections.singleton(filters.get("alarm-name")));
        }

        if (filters.containsKey("alarm-prefix") && !ObjectUtils.isBlank(filters.get("alarm-prefix"))) {
            builder = builder.alarmNamePrefix(filters.get("alarm-prefix"));
        }

        if (filters.containsKey("action-prefix") && !ObjectUtils.isBlank(filters.get("action-prefix"))) {
            builder = builder.actionPrefix(filters.get("action-prefix"));
        }

        if (filters.containsKey("state") && !ObjectUtils.isBlank(filters.get("state"))) {
            builder = builder.stateValue(filters.get("state"));
        }

        String marker = null;
        DescribeAlarmsRequest request;
        do {
            if (ObjectUtils.isBlank(marker)) {
                request = builder.build();
            } else {
                request = builder.nextToken(marker).build();
            }

            DescribeAlarmsResponse response = client.describeAlarms(request);
            marker = response.nextToken();
            metricAlarms.addAll(response.metricAlarms());

        } while (!ObjectUtils.isBlank(marker));

        return metricAlarms;
    }
}
