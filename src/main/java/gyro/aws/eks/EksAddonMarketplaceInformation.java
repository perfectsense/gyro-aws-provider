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

package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.eks.model.MarketplaceInformation;

public class EksAddonMarketplaceInformation extends Diffable implements Copyable<MarketplaceInformation> {

    private String productId;
    private String productUrl;

    /**
     * The product ID from AWS Marketplace.
     */
    @Output
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * The product URL from AWS Marketplace.
     */
    @Output
    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(MarketplaceInformation marketplaceInformation) {
        setProductId(marketplaceInformation.productId());
        setProductUrl(marketplaceInformation.productUrl());
    }
}
