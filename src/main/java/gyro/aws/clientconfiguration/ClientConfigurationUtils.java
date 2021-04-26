package gyro.aws.clientconfiguration;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import com.google.common.base.Preconditions;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;

public class ClientConfigurationUtils {

    private static long parse(String duration) {
        Preconditions.checkNotNull(duration);

        duration = (duration.startsWith("PT") ? duration : "PT" + duration).toUpperCase();

        return Duration.parse(duration).getSeconds();
    }

    public static Duration getDuration(String duration) {
        return Duration.ofSeconds(parse(duration));
    }

    public static void validate(String duration, String fieldName, String parent) {
        try {
            parse(duration);
        } catch (DateTimeParseException ex) {
            throw new GyroException(String.format(
                "%s'%s's time format '" + duration + "' is invalid.",
                (ObjectUtils.isBlank(parent) ? "" : String.format("%s: ", parent)),
                fieldName));
        }
    }
}
