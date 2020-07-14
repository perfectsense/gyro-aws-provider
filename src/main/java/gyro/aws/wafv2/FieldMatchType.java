package gyro.aws.wafv2;

public enum FieldMatchType {
    SINGLE_HEADER,
    SINGLE_QUERY_ARGUMENT,
    ALL_QUERY_ARGUMENTS,
    BODY,
    QUERY_STRING,
    METHOD,
    URI_PATH
}
