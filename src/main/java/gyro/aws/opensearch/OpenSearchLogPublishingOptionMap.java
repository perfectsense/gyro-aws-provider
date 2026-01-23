/*
 * Copyright 2026, Brightspot.
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

package gyro.aws.opensearch;

import gyro.core.resource.Diffable;
import gyro.core.resource.Id;

/**
 * Wrapper class to pair a log type name with its OpenSearchLogPublishingOption.
 */
public class OpenSearchLogPublishingOptionMap extends Diffable {

    private String name;
    private OpenSearchLogPublishingOption option;

    public OpenSearchLogPublishingOptionMap() {
    }

    public OpenSearchLogPublishingOptionMap(String name, OpenSearchLogPublishingOption option) {
        this.name = name;
        this.option = option;
    }

    /**
     * The log type name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The log publishing option configuration.
     *
     * @subresource gyro.aws.opensearch.OpenSearchLogPublishingOption
     */
    public OpenSearchLogPublishingOption getOption() {
        if (option == null) {
            option = new OpenSearchLogPublishingOption();
        }
        return option;
    }

    public void setOption(OpenSearchLogPublishingOption option) {
        this.option = option;
    }

    @Override
    public String primaryKey() {
        return name != null ? name : "";
    }
}
