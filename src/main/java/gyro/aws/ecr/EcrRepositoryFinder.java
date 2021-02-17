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

package gyro.aws.ecr;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.Repository;

/**
 * Query ECR repository.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecr-repository: $(external-query aws::ecr-repository { name: 'example-repo'})
 */
@Type("ecr-repository")
public class EcrRepositoryFinder extends AwsFinder<EcrClient, Repository, EcrRepositoryResource> {

    private String name;

    /**
     * The name of the repository.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Repository> findAllAws(EcrClient client) {
        return client.describeRepositories().repositories();
    }

    @Override
    protected List<Repository> findAws(EcrClient client, Map<String, String> filters) {
        return client.describeRepositories(r -> r.repositoryNames(filters.get("name"))).repositories();
    }
}
