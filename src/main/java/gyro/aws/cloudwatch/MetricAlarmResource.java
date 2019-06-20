package gyro.aws.cloudwatch;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an Auto scaling Group from a Launch Configuration or from a Launch Template.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::metric-alarm metric-alarm-example-1
 *         alarm-name: "metric-alarm-example-1"
 *         alarm-description: "metric-alarm-example-1"
 *         comparison-operator: "GreaterThanOrEqualToThreshold"
 *         threshold: 0.1
 *         evaluation-periods: 1
 *         metric-name: "CPUUtilization"
 *         period: 60
 *         namespace: "AWS/EC2"
 *         statistic: "SampleCount"
 *     end
 *
 *     aws::metric-alarm metric-alarm-example-2
 *         alarm-name: "metric-alarm-example-2"
 *         alarm-description: "metric-alarm-example-2"
 *         comparison-operator: "GreaterThanOrEqualToThreshold"
 *         threshold: 0.1
 *         evaluation-periods: 1
 *
 *         metric
 *             id: "e1"
 *             expression: "SUM(METRICS())"
 *             label: "Expression1"
 *             return-data: true
 *         end
 *
 *         metric
 *             id: "m1"
 *             metric-name: "CPUUtilization"
 *             namespace: "AWS/EC2"
 *             period: 120
 *             stat: "SampleCount"
 *             return-data: false
 *         end
 *     end
 */
@Type("metric-alarm")
public class MetricAlarmResource extends AwsResource implements Copyable<MetricAlarm> {

    private String alarmName;
    private Boolean actionsEnabled;
    private Set<String> alarmActions;
    private String alarmDescription;
    private String comparisonOperator;
    private Integer datapointsToAlarm;
    private Map<String, String> dimensions;
    private String evaluateLowSampleCountPercentile;
    private Integer evaluationPeriods;
    private String extendedStatistic;
    private Set<String> insufficientDataActions;
    private String metricName;
    private String namespace;
    private Set<String> okActions;
    private Integer period;
    private String statistic;
    private Double threshold;
    private String treatMissingData;
    private String unit;
    private String arn;
    private Set<MetricDataQueryResource> metric;
    private String state;

    /**
     * The name of the Metric Alarm. (Required)
     */
    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    /**
     * Indicates if actions are to be executed when reaches the Metric Alarm state. Defaults to true.
     */
    @Updatable
    public Boolean getActionsEnabled() {
        if (actionsEnabled == null) {
            actionsEnabled = true;
        }

        return actionsEnabled;
    }

    public void setActionsEnabled(Boolean actionsEnabled) {
        this.actionsEnabled = actionsEnabled;
    }

    /**
     * A set of actions to be executed when this Metric Alarm transitions to the ALARM state. Each action is a resource ARN.
     */
    @Updatable
    public Set<String> getAlarmActions() {
        if (alarmActions == null) {
            alarmActions = new HashSet<>();
        }

        return alarmActions;
    }

    public void setAlarmActions(Set<String> alarmActions) {
        this.alarmActions = alarmActions;
    }

    /**
     * A description for the Metric Alarm.
     */
    @Updatable
    public String getAlarmDescription() {
        return alarmDescription;
    }

    public void setAlarmDescription(String alarmDescription) {
        this.alarmDescription = alarmDescription;
    }

    /**
     * The operation to use when comparing using threshold and statistics. Valid values are ``GreaterThanOrEqualToThreshold`` or ``GreaterThanThreshold`` or ``LessThanThreshold`` or ``LessThanOrEqualToThreshold``.
     */
    @Updatable
    public String getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    /**
     * Number of data points to breach to trigger this Metric Alarm. Valid values [ Integer greater than 0 ].
     */
    @Updatable
    public Integer getDatapointsToAlarm() {
        return datapointsToAlarm;
    }

    public void setDatapointsToAlarm(Integer datapointsToAlarm) {
        this.datapointsToAlarm = datapointsToAlarm;
    }

    /**
     * Key Value pair to specify what the metric is as specified in the metric name. Max limit of 10.
     */
    @Updatable
    public Map<String, String> getDimensions() {
        if (dimensions == null) {
            dimensions = new HashMap<>();
        }

        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * This value indicates if less data points are present to evaluate a trigger, should it ignore or evaluate. Setting 'ignore' would ignore the data at that point. Valid values are ``evaluate`` or ``ignore``.
     */
    @Updatable
    public String getEvaluateLowSampleCountPercentile() {
        return evaluateLowSampleCountPercentile;
    }

    public void setEvaluateLowSampleCountPercentile(String evaluateLowSampleCountPercentile) {
        this.evaluateLowSampleCountPercentile = evaluateLowSampleCountPercentile;
    }

    /**
     * The number of period over which the data point's are evaluated. Valid values are any Integer greater than ``0``.
     */
    @Updatable
    public Integer getEvaluationPeriods() {
        return evaluationPeriods;
    }

    public void setEvaluationPeriods(Integer evaluationPeriods) {
        this.evaluationPeriods = evaluationPeriods;
    }

    /**
     * The percentile statistic for the metric specified in MetricName. Valid values are between ``p0.0`` and ``p100``.
     */
    @Updatable
    public String getExtendedStatistic() {
        return extendedStatistic;
    }

    public void setExtendedStatistic(String extendedStatistic) {
        this.extendedStatistic = extendedStatistic;
    }

    /**
     * A set of actions to execute when this Metric Alarm transitions to the INSUFFICIENT_DATA state. ach action is a resource ARN.
     */
    @Updatable
    public Set<String> getInsufficientDataActions() {
        if (insufficientDataActions == null) {
            insufficientDataActions = new HashSet<>();
        }

        return insufficientDataActions;
    }

    @Updatable
    public void setInsufficientDataActions(Set<String> insufficientDataActions) {
        this.insufficientDataActions = insufficientDataActions;
    }

    /**
     * The name of the metric associated with the Metric Alarm. Required if 'metric' not set. See `AWS Services That Publish CloudWatch Metrics <https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/aws-services-cloudwatch-metrics.html/>`_.
     */
    @Updatable
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * The namespace associated with the metric specified in 'metric-name' provided. Required if 'metric' not set.
     */
    @Updatable
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * A set of actions to execute when this Metric Alarm transitions to the OK state. ach action is a resource ARN.
     */
    @Updatable
    public Set<String> getOkActions() {
        if (okActions == null) {
            okActions = new HashSet<>();
        }

        return okActions;
    }

    public void setOkActions(Set<String> okActions) {
        this.okActions = okActions;
    }

    /**
     * The length, in seconds, used each time the metric specified in 'metric-name' is evaluated. Required if 'metric' not set.
     */
    @Updatable
    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    /**
     * The namespace associated with the metric specified in 'metric-name' provided. Valid values are ``SampleCount`` or ``Average`` or ``Sum`` or ``Minimum`` or ``Maximum``. Can only be set if 'metric' not set.
     */
    @Updatable
    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    /**
     * The value against which the specified statistic is compared.
     */
    @Updatable
    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    /**
     * How the metric handles missing data. Defaults to 'missing'. Valid values are ``breaching`` or ``notBreaching`` or ``ignore`` or ``missing``.
     */
    @Updatable
    public String getTreatMissingData() {
        if (treatMissingData == null) {
            treatMissingData = "missing";
        }

        return treatMissingData;
    }

    public void setTreatMissingData(String treatMissingData) {
        this.treatMissingData = treatMissingData;
    }

    /**
     * The unit of measure for the statistic.
     */
    @Updatable
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * A set of metric queries. If set 'metric-name', 'namespace', 'period' and 'dimensions' cannot be set.
     */
    @Updatable
    public Set<MetricDataQueryResource> getMetric() {
        if (metric == null) {
            metric = new HashSet<>();
        }

        return metric;
    }

    public void setMetric(Set<MetricDataQueryResource> metric) {
        this.metric = metric;
    }

    /**
     * The arn for the Metric Alarm.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The state of the Metric Alarm.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void copyFrom(MetricAlarm metricAlarm) {
        setActionsEnabled(metricAlarm.actionsEnabled());
        setAlarmActions(new HashSet<>(metricAlarm.alarmActions()));
        setAlarmDescription(metricAlarm.alarmDescription());
        setComparisonOperator(metricAlarm.comparisonOperator() != null ? metricAlarm.comparisonOperator().toString() : null);
        setDatapointsToAlarm(metricAlarm.datapointsToAlarm());
        setEvaluateLowSampleCountPercentile(metricAlarm.evaluateLowSampleCountPercentile());
        setEvaluationPeriods(metricAlarm.evaluationPeriods());
        setExtendedStatistic(metricAlarm.extendedStatistic());
        setInsufficientDataActions(new HashSet<>(metricAlarm.insufficientDataActions()));
        setMetricName(metricAlarm.metricName());
        setNamespace(metricAlarm.namespace());
        setOkActions(new HashSet<>(metricAlarm.okActions()));
        setPeriod(metricAlarm.period());
        setStatistic(metricAlarm.statistic() != null ? metricAlarm.statistic().toString() : null);
        setThreshold(metricAlarm.threshold());
        setTreatMissingData(metricAlarm.treatMissingData());
        setUnit(metricAlarm.unit() != null ? metricAlarm.unit().toString() : null);
        setArn(metricAlarm.alarmArn());
        setState(metricAlarm.stateValueAsString());

        for (Dimension dimension : metricAlarm.dimensions()) {
            getDimensions().put(dimension.name(), dimension.value());
        }

        getMetric().clear();
        for (software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery metricDataQuery : metricAlarm.metrics()) {
            MetricDataQueryResource metricDataQueryResource = newSubresource(MetricDataQueryResource.class);
            metricDataQueryResource.copyFrom(metricDataQuery);
            getMetric().add(metricDataQueryResource);
        }
    }

    @Override
    public boolean refresh() {
        CloudWatchClient client = createClient(CloudWatchClient.class);

        MetricAlarm metricAlarm = getMetricAlarm(client);

        if (metricAlarm == null) {
            return false;
        }

        copyFrom(metricAlarm);

        return true;
    }

    @Override
    public void create() {
        CloudWatchClient client = createClient(CloudWatchClient.class);

        saveMetricAlarm(client);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        CloudWatchClient client = createClient(CloudWatchClient.class);

        saveMetricAlarm(client);
    }

    @Override
    public void delete() {
        CloudWatchClient client = createClient(CloudWatchClient.class);

        client.deleteAlarms(r -> r.alarmNames(Collections.singleton(getAlarmName())));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("metric alarm");

        if (!ObjectUtils.isBlank(getAlarmName())) {
            sb.append(" - ").append(getAlarmName());
        }

        return sb.toString();
    }

    private MetricAlarm getMetricAlarm(CloudWatchClient client) {
        if (ObjectUtils.isBlank(getAlarmName())) {
            throw new GyroException("alarm-name is missing, unable to load metric alarm.");
        }

        try {
            DescribeAlarmsResponse response = client.describeAlarms(r -> r.alarmNames(Collections.singleton(getAlarmName())));

            if (response.metricAlarms().isEmpty()) {
                return null;
            }

            return response.metricAlarms().get(0);
        } catch (CloudWatchException ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                return null;
            }

            throw ex;
        }
    }

    private void saveMetricAlarm(CloudWatchClient client) {
        validate();

        PutMetricAlarmRequest.Builder builder = PutMetricAlarmRequest.builder();
        builder = builder.alarmName(getAlarmName())
            .actionsEnabled(getActionsEnabled())
            .alarmActions(getAlarmActions())
            .alarmDescription(getAlarmDescription())
            .comparisonOperator(getComparisonOperator())
            .datapointsToAlarm(getDatapointsToAlarm())
            .evaluateLowSampleCountPercentile(getEvaluateLowSampleCountPercentile())
            .evaluationPeriods(getEvaluationPeriods())
            .extendedStatistic(getExtendedStatistic())
            .insufficientDataActions(getInsufficientDataActions())
            .okActions(getOkActions())
            .threshold(getThreshold())
            .treatMissingData(getTreatMissingData())
            .unit(getUnit());

        if (!getMetric().isEmpty()) {
            builder = builder.metrics(getMetric().stream()
                .map(MetricDataQueryResource::getMetricDataQuery)
                .collect(Collectors.toList()));
        } else {
            builder = builder.metricName(getMetricName())
                .namespace(getNamespace())
                .period(getPeriod())
                .statistic(getStatistic())
                .dimensions(getDimensions().entrySet().stream().map(m -> Dimension.builder()
                    .name(m.getKey()).value(m.getValue()).build()).collect(Collectors.toList()));
        }

        client.putMetricAlarm(builder.build());
    }

    private void validate() {
        boolean flatMetricPresent = !ObjectUtils.isBlank(getNamespace()) || !ObjectUtils.isBlank(getPeriod())
            || !ObjectUtils.isBlank(getStatistic()) || !getDimensions().isEmpty();

        boolean metricSetPresent = !getMetric().isEmpty();

        if ((flatMetricPresent && metricSetPresent) || (!flatMetricPresent && !metricSetPresent)) {
            throw new GyroException("Either the param 'metric' or the collection of 'namespace', 'period', 'statistic' and optionally 'dimensions' must be specified. Not both together. ");
        }

        if (flatMetricPresent) {
            if (ObjectUtils.isBlank(getNamespace())) {
                throw new GyroException("The param 'namespace' is required when param 'metric' is not set.");
            }

            if (ObjectUtils.isBlank(getPeriod())) {
                throw new GyroException("The param 'period' is required when param 'metric' is not set.");
            }

            if (ObjectUtils.isBlank(getStatistic())) {
                throw new GyroException("The param 'statistic' is required when param 'metric' is not set.");
            }
        }
    }
}
