/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.clientconfiguration;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import com.google.common.base.Preconditions;

public class ClientConfigurationUtils {

    private static long parse(String duration) {
        Preconditions.checkNotNull(duration);

        duration = (duration.startsWith("PT") ? duration : "PT" + duration).toUpperCase();

        return Duration.parse(duration).getSeconds();
    }

    public static Duration getDuration(String duration) {
        return Duration.ofSeconds(parse(duration));
    }

    public static void validate(String duration, String fieldName) {
        try {
            parse(duration);
        } catch (DateTimeParseException ex) {
            throw new ClientConfigurationException(fieldName, String.format("Time format '%s' is invalid.", duration));
        } catch (NullPointerException npe) {
            throw new ClientConfigurationException(fieldName, "Is required.");
        }
    }
}
