package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateOptionGroupResponse;
import software.amazon.awssdk.services.rds.model.DescribeOptionGroupsResponse;
import software.amazon.awssdk.services.rds.model.OptionGroup;
import software.amazon.awssdk.services.rds.model.OptionGroupNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a db option group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::db-option-group db-option-group-example
 *        name: "option-group-example"
 *        description: "option group example"
 *        engine: "mysql"
 *        major-engine-version: "5.6"
 *
 *        option
 *            option-name: "MARIADB_AUDIT_PLUGIN"
 *
 *            option-settings
 *                name: "SERVER_AUDIT_FILE_ROTATIONS"
 *                value: "20"
 *            end
 *
 *            option-settings
 *                name: "SERVER_AUDIT_FILE_ROTATE_SIZE"
 *                value: "1000"
 *            end
 *        end
 *
 *        tags: {
 *            Name: "db-option-group-example"
 *        }
 *    end
 */
@Type("db-option-group")
public class DbOptionGroupResource extends RdsTaggableResource implements Copyable<OptionGroup> {

    private String name;
    private String description;
    private String engine;
    private String majorEngineVersion;
    private List<OptionConfiguration> option;

    /**
     * The name of the option group.
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the option group.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the engine that this option group should be associated with.
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The major version of the engine that this option group should be associated with.
     */
    public String getMajorEngineVersion() {
        return majorEngineVersion;
    }

    public void setMajorEngineVersion(String majorEngineVersion) {
        this.majorEngineVersion = majorEngineVersion;
    }

    /**
     * A list of options to apply.
     *
     * @subresource gyro.aws.rds.OptionConfiguration
     */
    @Updatable
    public List<OptionConfiguration> getOption() {
        if (option == null) {
            option = new ArrayList<>();
        }

        return option;
    }

    public void setOption(List<OptionConfiguration> option) {
        this.option = option;
    }

    @Override
    public void copyFrom(OptionGroup group) {
        setDescription(group.optionGroupDescription());
        setEngine(group.engineName());
        setMajorEngineVersion(group.majorEngineVersion());
        setOption(group.options().stream()
            .map(o -> {
                OptionConfiguration current = getOption().stream().filter(
                    c -> c.getOptionName().equals(o.optionName())).findFirst().orElse(new OptionConfiguration());
                OptionConfiguration optionConfiguration = new OptionConfiguration();
                optionConfiguration.setOptionName(o.optionName());
                optionConfiguration.setPort(o.port());
                optionConfiguration.setVersion(o.optionVersion());

                optionConfiguration.setVpcSecurityGroups(
                    o.vpcSecurityGroupMemberships().stream()
                        .map(g -> findById(SecurityGroupResource.class, g.vpcSecurityGroupId()))
                        .collect(Collectors.toSet()));

                optionConfiguration.setOptionSettings(o.optionSettings().stream()
                    .filter(s -> current.getOptionSettings().stream()
                        .map(OptionSettings::getName)
                        .collect(Collectors.toSet()).contains(s.name()))
                    .map(s -> {
                        OptionSettings optionSettings = new OptionSettings();
                        optionSettings.setName(s.name());
                        optionSettings.setValue(s.value());
                        return optionSettings;
                    })
                    .collect(Collectors.toSet()));

                return optionConfiguration;
            })
            .collect(Collectors.toList()));

        setArn(group.optionGroupArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db option group.");
        }

        try {
            DescribeOptionGroupsResponse response = client.describeOptionGroups(
                r -> r.optionGroupName(getName())
            );

            response.optionGroupsList().forEach(this::copyFrom);

        } catch (OptionGroupNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateOptionGroupResponse response = client.createOptionGroup(
            r -> r.engineName(getEngine())
                    .optionGroupDescription(getDescription())
                    .majorEngineVersion(getMajorEngineVersion())
                    .optionGroupName(getName())
        );

        setArn(response.optionGroup().optionGroupArn());
        modifyOptionGroup(new ArrayList<>());
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {

        DbOptionGroupResource current = (DbOptionGroupResource) config;
        List<OptionConfiguration> removeList = current.getOption().stream()
            .filter(o -> !getOption().stream()
                .map(OptionConfiguration::getOptionName)
                .collect(Collectors.toList())
                .contains(o.getOptionName()))
            .collect(Collectors.toList());

        modifyOptionGroup(removeList);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteOptionGroup(
            r -> r.optionGroupName(getName())
        );
    }

    private void modifyOptionGroup(List<OptionConfiguration> removeList) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyOptionGroup(
            r -> r.optionGroupName(getName())
                .optionsToInclude(getOption().stream()
                    .map(o -> software.amazon.awssdk.services.rds.model.OptionConfiguration.builder()
                        .optionName(o.getOptionName())
                        .optionVersion(o.getVersion())
                        .port(o.getPort())
                        .vpcSecurityGroupMemberships(o.getVpcSecurityGroups()
                            .stream()
                            .map(SecurityGroupResource::getId)
                            .collect(Collectors.toList()))
                        .optionSettings(o.getOptionSettings().stream()
                            .map(s -> software.amazon.awssdk.services.rds.model.OptionSetting.builder()
                                .name(s.getName())
                                .value(s.getValue())
                                .build())
                            .collect(Collectors.toList()))
                        .build())
                    .collect(Collectors.toList()))

                .optionsToRemove(removeList.stream()
                    .map(OptionConfiguration::getOptionName)
                    .collect(Collectors.toList()))
        );
    }
}
