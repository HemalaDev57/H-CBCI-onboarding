package io.jenkins.plugins.sample;


import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import net.sf.json.JSONObject;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;

@Extension
public class OnboardingPluginConfig extends GlobalConfiguration {

    private String name;

    private String description;

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

}