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
 *     {@literal @}plugin: 'gyro:gyro-aws-provider:0.99.3'
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
 * State Locking
 * +++++++++++++
 *
 * This provider uses DynamoDb for state locking. In order to use DynamoDb for locking, you must create a table
 * with a **primary key** titled ``LockKey``. Then define the lock backend in ``.gyro/init.gyro`` with its
 * ``table-name`` in your Gyro project:
 *
 * .. code:: shell
 *
 *     {@literal @}lock-backend 'aws::dynamo-db'
 *         table-name: 'gyro-lock-table'
 *     {@literal @}end
 *
 * If you want to use this same DynamoDb table for multiple Gyro projects, specify the optional ``lock-key`` field.
 * This field must have a unique value per project as it is the ID that is used to ensure only one lock per project
 * exists at a time.
 *
 * .. code:: shell
 *
 *     {@literal @}lock-backend 'aws::dynamo-db'
 *         table-name: 'gyro-lock-table'
 *         lock-key: 'GyroProject1Key'
 *     {@literal @}end
 *
 * You may also specify a ``credentials`` field if you would like to use named credentials other than the ``default``
 * credentials:
 *
 * .. code:: shell
 *
 *     {@literal @}lock-backend 'aws::dynamo-db'
 *         table-name: 'gyro-lock-table'
 *         credentials: 'us-east-2'
 *     {@literal @}end
 *
 * Remote State Storage
 * ++++++++++++++++++++
 *
 * This provider uses S3 for remote state storage. In order to use S3 for remote state storage, either use an existing
 * bucket or create a new one. Next, add the state backend to your ``.gyro/init.gyro`` with its ``table-name``, and
 * optional prefix (we recommend using ``.gyro/state`` as your prefix):
 *
 * .. code:: shell
 *
 *     {@literal @}state-backend 'aws::s3'
 *         bucket: 'gyro-state-bucket'
 *         prefix: '.gyro/state'
 *     {@literal @}end
 *
 */
@DocNamespace("aws")
@Namespace("aws")
package gyro.aws;

import gyro.core.Namespace;
import gyro.core.resource.DocNamespace;
