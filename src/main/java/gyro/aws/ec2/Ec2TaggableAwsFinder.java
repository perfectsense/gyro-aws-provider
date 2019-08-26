package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import software.amazon.awssdk.core.SdkClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Ec2TaggableAwsFinder<C extends SdkClient, M, R extends AwsResource> extends AwsFinder<C, M, R> {

    @Override
    public final List<R> find(Map<String, Object> filters) {
        return findAws(newClient(), convertTags(filters)).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    /**
     * Convert {tagKey: tagValue} to {tag:Key: tagValue}
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> convertTags(Map<String, Object> query) {
        Map<String, String> filters = new HashMap<>();

        for (Map.Entry<String, Object> e : query.entrySet()) {
            if ("tag".equalsIgnoreCase(e.getKey()) && e.getValue() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) e.getValue();
                for (String key : map.keySet()) {
                    filters.put("tag:" + key, map.get(key).toString());
                }

            } else if (!(e.getValue() instanceof String)) {
                throw new GyroException("Unsupported type in filter: " + e.getValue().getClass());
            } else {
                filters.put(e.getKey(), e.getValue().toString());
            }
        }

        return filters;
    }
}
