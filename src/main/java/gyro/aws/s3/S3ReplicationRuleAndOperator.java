package gyro.aws.s3;

import com.psddev.dari.util.CompactMap;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.ReplicationRuleAndOperator;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S3ReplicationRuleAndOperator extends Diffable implements Copyable<ReplicationRuleAndOperator> {
    private String prefix;
    private List<S3Tag> tag;

    /**
     * Object prefix that this rule applies to. (Required)
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * List of tags to select the objects that will be replicated (Required)
     *
     * @subresource aws.aws.s3.S3Tag
     */
    @Updatable
    public List<S3Tag> getTag() {
        if(tag == null){
            tag =  new ArrayList<>();
        }
        return tag;
    }

    public void setTag(List<S3Tag> tag) {
        this.tag = tag;
    }

    @Override
    public void copyFrom(ReplicationRuleAndOperator replicationRuleAndOperator) {
        setPrefix(replicationRuleAndOperator.prefix());

        if(replicationRuleAndOperator.tags() != null){
            for (Tag tag : replicationRuleAndOperator.tags()){
                S3Tag s3tag = newSubresource(S3Tag.class);
                s3tag.copyFrom(tag);
                getTag().add(s3tag);
            }
        }
    }

    ReplicationRuleAndOperator toReplicationRuleAndOperator(){
        ReplicationRuleAndOperator.Builder builder = ReplicationRuleAndOperator.builder();
        builder.prefix(getPrefix());

        if(!getTag().isEmpty()){
            builder.tags(getTag().stream().map(S3Tag::toTag).collect(Collectors.toList()));
        }

        return builder.build();
    }

    @Override
    public String primaryKey() {
        return "replication rule and operator";
    }
}
