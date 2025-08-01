package gyro.aws.pulumi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.pulumi.automation.ConfigValue;
import com.pulumi.automation.LocalWorkspace;
import com.pulumi.automation.PreviewResult;
import com.pulumi.automation.UpResult;
import com.pulumi.automation.UpdateResult;
import com.pulumi.automation.WorkspaceStack;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.utils.IoUtils;

@Type("pulumi")
public class PulumiAwsResource extends AwsResource {
    private String stackName;
    private String relativePath;
    private String profileName;
    private String config;

    @Id
    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @Updatable
    @Required
    public String getConfig() {
        if (this.config != null && this.config.endsWith(".yaml")) {
            try (InputStream input = openInput(this.config)) {
                this.config = formatYamlFile(IoUtils.toUtf8String(input));
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        }

        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public boolean refresh() {
        Path path = Paths.get(getRelativePath());

        try (WorkspaceStack stack = LocalWorkspace.createOrSelectStack(getStackName(), path)) {
            PreviewResult preview = stack.preview();
            //            GyroUI ui = GyroCore.ui();
            //            preview.changeSummary()
            //                .forEach((key, value) -> System.out.print("\n @|bold,blue  Type: " + key + ": " + value + " |@"));
            //
            //            System.out.print("\n @|bold,blue  Preview Standard Output: " + preview.standardOutput() + " |@");

            UpdateResult refresh = stack.refresh();
            //            System.out.print("\n @|bold,blue  Refresh Resource changes: " + refresh.summary().resourceChanges() + " |@");
            //
            //            System.out.print("\n @|bold,blue  Refresh Standard Output: " + refresh.standardOutput() + " |@");

        } catch (Exception e) {
            throw new GyroException(e);
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Path path = Paths.get(getRelativePath());

        try (WorkspaceStack stack = LocalWorkspace.createOrSelectStack(getStackName(), path)) {

            // Set configuration values
            ui.write("\n @|bold,blue  Setting config... |@");
            stack.setConfig("aws:region", new ConfigValue(getRegion()));
            stack.setConfig("aws:profile", new ConfigValue(getProfileName()));

            ui.write("\n @|bold,blue  Running pulumi up... |@");
            UpResult result = stack.up();

            ui.write("\n @|bold,blue  Resources deployed: " + result.summary().resourceChanges() + " |@");
            ui.write("\n @|bold,blue  Standard Output: " + result.standardOutput() + " |@");
            result.outputs()
                .forEach((key, value) -> ui.write(
                    "\n @|bold,blue  Output: " + key + ": " + value.value().toString() + " |@"));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        Path path = Paths.get(getRelativePath());

        try (WorkspaceStack stack = LocalWorkspace.createOrSelectStack(getStackName(), path)) {
            ui.write("\n @|bold,blue  Running pulumi up... |@");
            UpResult result = stack.up();

            ui.write("\n @|bold,blue  Resources deployed: " + result.summary().resourceChanges() + " |@");
            ui.write("\n @|bold,blue  Standard Output: " + result.standardOutput() + " |@");
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Path path = Paths.get(getRelativePath());

        try (WorkspaceStack stack = LocalWorkspace.createOrSelectStack(getStackName(), path)) {
            ui.write("\n @|bold,blue  Running pulumi up... |@");
            UpdateResult result = stack.destroy();

            ui.write("\n @|bold,blue  Resource Changes: " + result.summary().resourceChanges() + " |@");
            ui.write("\n @|bold,blue  Standard Output: " + result.standardOutput() + " |@");
        }
    }

    private String getRegion() {
        AwsCredentials credentials = credentials(AwsCredentials.class);
        return credentials.getRegion();
    }

    public static String formatYamlFile(String file) {
        String formattedFile = null;

        if (file != null) {
            boolean quoted = false;
            StringBuilder out = new StringBuilder();

            for (Character c : file.toCharArray()) {
                if (c == '"') {
                    quoted = !quoted;
                }

                if (c != System.lineSeparator().charAt(0) && c != '\t') {
                    if (c != ' ' || quoted) {
                        out.append(c);
                    }
                }
            }
            formattedFile = out.toString();
        }

        return formattedFile;
    }

    public static String formatYamlFileWithSpace(String file) {
        String formattedFile = null;

        if (file != null) {
            StringBuilder out = new StringBuilder();

            for (Character c : file.toCharArray()) {
                out.append(c);
            }
            formattedFile = out.toString();
        }

        return formattedFile;
    }
}
