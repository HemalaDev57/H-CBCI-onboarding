package io.jenkins.plugins.sample;


import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONObject;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;

@Extension
public class OnboardingPluginConfig extends GlobalConfiguration {

    private String name;

    private String description;

    private boolean connectionConfig;

    private String userName;

    private Secret password;

    public OnboardingPluginConfig() {
        load();
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        if (name.matches("^[a-zA-Z ]+$")) {
            this.name = name;
            save();
        }
    }

    public boolean getConnectionConfig() {
        return connectionConfig;
    }

    @DataBoundSetter
    public void setConnectionConfig(boolean connectionConfig) {
        this.connectionConfig = connectionConfig;
        save();
    }

    public String getUserName() {
        return userName;
    }

    @DataBoundSetter
    public void setUserName(String userName) {
        this.userName = userName;
        save();
    }

    public Secret getPassword() {
        return password;
    }

    @DataBoundSetter
    public void setPassword(Secret password) {
        this.password = password;
        save();
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public FormValidation doCheckName(@QueryParameter String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set the name");
        }
        if (!value.matches("^[a-zA-Z ]+$")) {
            return FormValidation.error("Invalid format. The name should only contain lowercase, uppercase letters and "
                                        + "spaces");
        }
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        String submittedName = json.getString("name");
        if (!submittedName.matches("^[a-zA-Z ]+$")) {
            throw new FormException("Invalid name. Data not saved.", "name");
        }
        this.name = submittedName;
        this.description = json.getString("description");

        if (json.containsKey("connectionConfig")) {
            JSONObject block = json.getJSONObject("connectionConfig");
            this.connectionConfig = true;
            this.userName = block.getString("userName");
            // Converting plain text string to Secret object
            this.password = Secret.fromString(block.getString("password"));
        } else {
            this.connectionConfig = false;
            this.userName = null;
            this.password = null;
        }
        save();
        return true;
    }

}