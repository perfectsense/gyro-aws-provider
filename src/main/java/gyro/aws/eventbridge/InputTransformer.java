/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class InputTransformer extends Diffable implements Copyable<software.amazon.awssdk.services.eventbridge.model.InputTransformer> {

    private Map<String, String> inputPathsMap;
    private String inputTemplate;

    /**
     * Map of JSON paths to be extracted from the event.
     */
    @Required
    @Updatable
    public Map<String, String> getInputPathsMap() {
        if (inputPathsMap == null) {
            inputPathsMap = new HashMap<>();
        }

        return inputPathsMap;
    }

    public void setInputPathsMap(Map<String, String> inputPathsMap) {
        this.inputPathsMap = inputPathsMap;
    }

    /**
     * Input template where you specify placeholders that will be filled with the values of the keys from input-paths-map to customize the data sent to the target.
     */
    @Required
    @Updatable
    public String getInputTemplate() {
        return inputTemplate;
    }

    public void setInputTemplate(String inputTemplate) {
        this.inputTemplate = inputTemplate;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.eventbridge.model.InputTransformer model) {
        setInputPathsMap(model.inputPathsMap());
        setInputTemplate(model.inputTemplate());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected software.amazon.awssdk.services.eventbridge.model.InputTransformer toInputTransformer() {
        return software.amazon.awssdk.services.eventbridge.model.InputTransformer
            .builder()
            .inputPathsMap(getInputPathsMap())
            .inputTemplate(getInputTemplate())
            .build();
    }
}
