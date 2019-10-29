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

package gyro.aws.cloudwatch;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricDataQueryResource extends Diffable implements Copyable<MetricDataQuery> {

    private String id;
    private String expression;
    private String label;
    private Boolean returnData;
    private String metricName;
    private String namespace;
    private String stat;
    private String unit;
    private Integer period;
    private Map<String, String> dimensions;

    /**
     * The ID for the metric.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The math expression to be performed on the returned data, if this object is performing a math expression for the metric.
     */
    @Updatable
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * The label for this metric.
     */
    @Updatable
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * The name of the metric.
     */
    @Updatable
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * The namespace of the metric.
     */
    @Updatable
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The statistic to return for the metric.
     */
    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    /**
     * The unit to use for the returned data points for the metric.
     */
    @Updatable
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * The period, in seconds, to use when retrieving the metric.
     */
    @Updatable
    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    /**
     * The dimensions for the metric.
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
     * Indicates whether to return the timestamps and raw data values of this metric.
     */
    @Updatable
    public Boolean getReturnData() {
        if (returnData == null) {
            returnData = false;
        }

        return returnData;
    }

    public void setReturnData(Boolean returnData) {
        this.returnData = returnData;
    }

    @Override
    public String primaryKey() {
        return getId();
    }

    @Override
    public void copyFrom(MetricDataQuery metricDataQuery) {
        setId(metricDataQuery.id());
        setExpression(metricDataQuery.expression());
        setReturnData(metricDataQuery.returnData());
        setLabel(metricDataQuery.label());

        if (metricDataQuery.metricStat() != null) {
            if (metricDataQuery.metricStat().metric() != null) {
                setMetricName(metricDataQuery.metricStat().metric().metricName());
                setNamespace(metricDataQuery.metricStat().metric().namespace());

                for (Dimension dimension : metricDataQuery.metricStat().metric().dimensions()) {
                    getDimensions().put(dimension.name(), dimension.value());
                }
            }

            setPeriod(metricDataQuery.metricStat().period());
            setStat(metricDataQuery.metricStat().stat());
            setUnit(metricDataQuery.metricStat().unitAsString());
        }
    }

    MetricDataQuery getMetricDataQuery() {
        MetricDataQuery.Builder builder =  MetricDataQuery.builder();

        builder = builder.expression(getExpression())
            .id(getId())
            .label(getLabel())
            .returnData(getReturnData());

        if (!ObjectUtils.isBlank(getMetricName())) {
            builder = builder.metricStat(
                m -> m.period(getPeriod())
                    .stat(getStat())
                    .unit(getUnit())
                    .metric(mm -> mm.namespace(getNamespace())
                        .metricName(getMetricName())
                        .dimensions(
                            getDimensions().entrySet().stream()
                                .map(d -> Dimension.builder().name(d.getKey()).value(d.getValue()).build())
                                .collect(Collectors.toList()))
                    )
            );
        }

        return builder.build();
    }
}
