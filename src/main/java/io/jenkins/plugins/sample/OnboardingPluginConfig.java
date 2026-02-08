package io.jenkins.plugins.sample;


import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
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
        this.name = name;
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

}