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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CaseFormat;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.Reflections;
import gyro.core.Type;
import gyro.core.directive.DirectiveProcessor;
import gyro.core.scope.RootScope;
import gyro.core.scope.Scope;
import gyro.lang.ast.block.DirectiveNode;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

@Type("client-configuration")
public class ClientConfigurationDirectiveProcessor extends DirectiveProcessor<RootScope> {

    @Override
    public void process(RootScope scope, DirectiveNode node) throws Exception {
        validateArguments(node, 0, 1);

        String name = getArgument(scope, node, String.class, 0);

        if (ObjectUtils.isBlank(name)) {
            name = "default";
        }

        Scope bodyScope = evaluateBody(scope, node);

        ClientConfigurationSettings settings = scope.getSettings(ClientConfigurationSettings.class);

        ClientConfiguration clientConfiguration = (ClientConfiguration) process(
            ClientConfiguration.class,
            scope,
            bodyScope);

        settings.getClientConfigurations().put(name, clientConfiguration);
    }

    // Automate config consumption mapping fields to class parameters
    private Object process(Class<?> configClass, RootScope scope, Scope bodyScope) {
        Object configClassObj = Reflections.newInstance(configClass);
        if (bodyScope != null) {
            for (PropertyDescriptor property : Reflections.getBeanInfo(configClass).getPropertyDescriptors()) {
                Method setter = property.getWriteMethod();
                if (setter != null) {
                    java.lang.reflect.Type genericParameterType = setter.getGenericParameterTypes()[0];
                    Object configVal = bodyScope.get(CaseFormat.LOWER_CAMEL.to(
                        CaseFormat.LOWER_HYPHEN,
                        property.getName()));

                    // Recursively assign values for complex gyro types
                    if (genericParameterType.getTypeName().contains("gyro")) {
                        if (configVal instanceof List) {
                            if (genericParameterType.getTypeName().contains("java.util.List")) {
                                if (genericParameterType instanceof ParameterizedTypeImpl) {
                                    List<?> configVals = (List<?>) configVal;
                                    List<Object> processedConfigs = new ArrayList<>();
                                    for (Object obj : configVals) {
                                        if (obj instanceof Scope) {
                                            processedConfigs.add(process(
                                                (Class<?>) ((ParameterizedTypeImpl) genericParameterType).getActualTypeArguments()[0],
                                                scope,
                                                (Scope) obj));
                                        }
                                    }
                                    configVal = processedConfigs;
                                }
                            } else if (((List<?>) configVal).size() > 0) {
                                Object obj = ((List<?>) configVal).get(0);
                                if (obj instanceof Scope) {
                                    configVal = process(setter.getParameterTypes()[0], scope, (Scope) obj);
                                }
                            }
                        }
                    }

                    if (configVal != null) {
                        Reflections.invoke(setter, configClassObj, scope.convertValue(
                            setter.getGenericParameterTypes()[0],
                            configVal));
                    }
                }
            }
        }

        if (ClientConfigurationInterface.class.isAssignableFrom(configClass)) {
            ((ClientConfigurationInterface) configClassObj).validate();
        }

        return configClassObj;
    }
}
