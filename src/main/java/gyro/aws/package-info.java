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

/**
 * AWS
 * ===
 *
 * The AWS provider implements support for Amazon Web Services cloud provider.
 *
 * Usage
 * +++++
 *
 * The AWS provider is implemented as a plugin. To use it add the plugin to your init file.
 * It uses the format ``@plugin: gyro:gyro-aws-provider:<version>``.
 *
 * .. code:: shell
 *
 *     {@literal @}repository: 'https://artifactory.psdops.com/gyro-releases'
 *     {@literal @}plugin: 'gyro:gyro-aws-provider:0.99.0'
 *
 * This lets Gyro load the AWS provider plugin and lets you start managing AWS resources using Gyro.
 *
 * Authentication
 * ++++++++++++++
 *
 * This provider expects credentials to be provided using the same mechanism that
 * the AWS CLI uses.
 *
 * First, define your credentials in ``$HOME/.aws/credentials`` under a profile name
 * of your chosing:
 *
 * .. code:: shell
 *
 *     [my-project]
 *     aws_secret_access_key = jIk7vCcAIm9zb0LPizhqjKrjGm7HF47VglVoFnjS
 *     aws_access_key_id = IPPL6D4B4JCBO8HBIMJG
 *
 * Then define these credentials in ``.gyro/init.gyro`` in your Gyro project along with
 * the region you want to use these credentials in.
 *
 * .. code:: shell
 *
 *     {@literal @}credentials 'aws::credentials'
 *         profile-name: 'my-project'
 *         region: 'us-east-1'
 *     {@literal @}end
 *
 * To use more than one region, provide a name for your credentials. When a name is not provided
 * then the credentials because the ``default``.
 *
 * .. code:: shell
 *
 *     {@literal @}credentials 'aws::credentials' us-east-2
 *         profile-name: 'my-project'
 *         region: 'us-east-2'
 *     {@literal @}end
 *
 * To use a non-default set of credentials you must explicitly use them in your resource definitions:
 *
 * .. code:: shell
 *
 *     aws::instance web-server
 *         instance-type: 't2.micro'
 *
 *         {@literal @}uses-credentials: 'us-east-2'
 *     end
 *
 */
@DocNamespace("aws")
@Namespace("aws")
package gyro.aws;

import gyro.core.Namespace;
import gyro.core.resource.DocNamespace;
