package gyro.aws.cloudfront;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionList;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchDistributionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cloudfront.
 *
 * .. code-block:: gyro
 *
 *    cloudfront: $(aws::cloudfront EXTERNAL/* | id = '')
 */
@Type("cloudfront")
public class CloudFrontFinder extends AwsFinder<CloudFrontClient, Distribution, CloudFrontResource> {
    private String id;

    /**
     * The ID of the cloudfront distribution.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Distribution> findAllAws(CloudFrontClient client) {
        List<Distribution> distributions = new ArrayList<>();
        List<String> distributionIds = new ArrayList<>();
        String marker = null;
        DistributionList distributionList;

        do {
            if (marker == null) {
                distributionList = client.listDistributions().distributionList();
            } else {
                distributionList = client.listDistributions(ListDistributionsRequest.builder().marker(marker).build()).distributionList();
            }
            distributionIds.addAll(distributionList.items().stream().map(DistributionSummary::id).collect(Collectors.toList()));

            marker = distributionList.marker();
        } while (distributionList.isTruncated());

        distributionIds.forEach(o -> distributions.add(client.getDistribution(r -> r.id(o)).distribution()));

        return distributions;
    }

    @Override
    protected List<Distribution> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<Distribution> distributions = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                distributions.add(client.getDistribution(r -> r.id(filters.get("id"))).distribution());
            } catch (NoSuchDistributionException ignore) {
                // ignore
            }
        }

        return distributions;
    }

    @Override
    protected String getRegion() {
        return "us-east-1";
    }

    @Override
    protected String getEndpoint() {
        return "https://cloudfront.amazonaws.com";
    }
}
