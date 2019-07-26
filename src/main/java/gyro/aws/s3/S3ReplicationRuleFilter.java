package gyro.aws.s3;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.ReplicationRuleFilter;

import java.util.*;

public class S3ReplicationRuleFilter extends Diffable implements Copyable<ReplicationRuleFilter> {
    private String prefix;
    private S3Tag tag;
    private S3ReplicationRuleAndOperator andOperator;

    /**
     * Object prefix that this rule applies to.
     */
   @Updatable
    public String getPrefix(){
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Container that applies all filters to select object.
     *
     * @subresource gyro.aws.s3.S3ReplicationRuleAndOperator
     */
    @Updatable
    public S3ReplicationRuleAndOperator getAndOperator() {
        return andOperator;
    }

    public void setAndOperator(S3ReplicationRuleAndOperator andOperator) {
        this.andOperator = andOperator;
    }


    /**
     * Tag used to select the objects that will be replicated
     *
     * @subresource aws.aws.s3.S3Tag
     */
    @Updatable
    public S3Tag getTag() {
        return tag;
    }

    public void setTag(S3Tag tag) {
        this.tag = tag;
    }

    @Override
    public void copyFrom(ReplicationRuleFilter replicationRuleFilter){
        if(replicationRuleFilter.prefix() != null){
            setPrefix(replicationRuleFilter.prefix());
        }

        if(replicationRuleFilter.tag() != null){
            S3Tag tag = newSubresource(S3Tag.class);
            tag.copyFrom(replicationRuleFilter.tag());
            setTag(tag);
        }

        if(replicationRuleFilter.and() != null){
            S3ReplicationRuleAndOperator and = newSubresource(S3ReplicationRuleAndOperator.class);
            and.copyFrom(replicationRuleFilter.and());
            setAndOperator(and);
        }
    }


    @Override
    public String toDisplayString() {
        return "replication rule filter";
    }

    @Override
    public String primaryKey() {
        return toDisplayString();
    }

    ReplicationRuleFilter toReplicationRuleFilter(){
        ReplicationRuleFilter.Builder builder = ReplicationRuleFilter.builder();

        if(getPrefix() != null && !ObjectUtils.isBlank(getPrefix())){
            builder.prefix(getPrefix());
        } else if (getTag() != null){
            builder.tag(getTag().toTag());
        } else if (getAndOperator() != null){
            builder.and(getAndOperator().toReplicationRuleAndOperator());
        }

        return builder.build();
    }

    private Map<String, String> fromTags(List<Tag> tags){
        Map<String, String> tagMap = new HashMap<>();
        for (Tag tag : tags){
            tagMap.put(tag.key(), tag.value());
        }

        return tagMap;
    }

    private Tag toTag(Map<String, String> tagMap){
        for(String key : tagMap.keySet()){
            return Tag.builder()
                    .key(key)
                    .value(tagMap.get(key))
                    .build();
        }

        return null;
    }

    private List<Tag> toTags(Map<String, String> tagMap){
        List<Tag> tags = new ArrayList<>();

        for(String key : tagMap.keySet()){
            tags.add( Tag.builder()
                    .key(key)
                    .value(tagMap.get(key))
                    .build()
            );
        }

        return tags;
    }
}
