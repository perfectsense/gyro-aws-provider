package gyro.aws.waf.common;

import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.waf.model.Predicate;

import java.util.List;
import java.util.Set;

@Type("common-rule")
public class CommonRuleResource extends AbstractRuleResource {
    @Override
    public boolean isRateRule() {
        return false;
    }

    @Override
    protected void loadPredicates(List<Predicate> predicates) {

    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, Context context) {

    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, Context context) {

    }

    protected String getType() {
        return null;
    }
}
