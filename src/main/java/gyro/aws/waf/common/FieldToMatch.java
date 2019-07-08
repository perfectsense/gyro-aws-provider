package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;

public class FieldToMatch extends Diffable implements Copyable<software.amazon.awssdk.services.waf.model.FieldToMatch> {
    private String data;
    private String type;

    /**
     * If type selected as ``HEADER`` or ``SINGLE_QUERY_ARG``, the value needs to be provided.
     */
    public String getData() {
        return data != null ? data.toLowerCase() : null;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Part of the request to filter on. Valid values are ``URI`` or ``QUERY_STRING`` or ``HEADER`` or ``METHOD`` or ``BODY`` or ``SINGLE_QUERY_ARG`` or ``ALL_QUERY_ARGS``. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.waf.model.FieldToMatch fieldToMatch) {
        setData(fieldToMatch.data());
        setType(fieldToMatch.typeAsString());
    }

    @Override
    public String primaryKey() {
        return "field to match";
    }

    @Override
    public String toDisplayString() {
        return "field to match";
    }

    software.amazon.awssdk.services.waf.model.FieldToMatch toFieldToMatch() {
        return software.amazon.awssdk.services.waf.model.FieldToMatch.builder().type(getType()).data(getData()).build();
    }
}
