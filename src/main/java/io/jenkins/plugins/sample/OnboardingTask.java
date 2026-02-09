package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.io.IOException;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

public class OnboardingTask extends Builder {

    private final String categoryUuid;

    @DataBoundConstructor
    public OnboardingTask(String categoryUuid) {
        this.categoryUuid = categoryUuid;
    }

    public String getCategoryUuid() {
        return categoryUuid;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        OnboardingPluginConfig config = GlobalConfiguration.all().get(OnboardingPluginConfig.class);

        assert config != null;
        String categoryName = config.getCategories().stream()
                .filter(c -> c.getUuid().equals(categoryUuid))
                .map(OnboardingPluginConfig.Category::getCategoryName)
                .findFirst()
                .orElse("Unknown Category");

        listener.getLogger().println("Selected Category: " + categoryName);

        BuildHistory history = BuildHistory.load();
        history.updateCategoryJob(this.categoryUuid, build.getParent().getFullName());
        history.addRecord(build.getParent().getFullDisplayName(), build.getNumber(), categoryName);

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return "hudson.model.FreeStyleProject".equals(aClass.getName());
        }

        @Override
        public String getDisplayName() {
            return "Onboarding Task";
        }

        public ListBoxModel doFillCategoryUuidItems() {
            ListBoxModel items = new ListBoxModel();
            OnboardingPluginConfig config = GlobalConfiguration.all().get(OnboardingPluginConfig.class);

            if (config != null && config.getCategories() != null) {
                for (OnboardingPluginConfig.Category c : config.getCategories()) {
                    items.add(c.getCategoryName(), c.getUuid());
                }
            }
            return items;
        }
    }
}
