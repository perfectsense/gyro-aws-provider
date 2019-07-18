package gyro.aws.waf.common;

import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;

import java.util.Set;

@Type("condition")
public class ConditionResource extends AbstractWafResource {
    private String id;
    private String name;

    String getDisplayName() {
        return "";
    }

    /**
     * The id of the condition.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {

    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
    }

    @Override
    public void delete(GyroUI ui, State state) {

    }

    protected String getType() {
        return null;
    }
}
