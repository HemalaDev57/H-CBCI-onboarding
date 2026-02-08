package io.jenkins.plugins.sample;


import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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

    private Secret payload;

    public Secret getPayload() {
        return payload;
    }

    @DataBoundSetter
    public void setPayload(Secret payload) {
        this.payload = payload;
    }

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

    public FormValidation doTestConnection(
            @QueryParameter("userName") String userName,
            @QueryParameter("password") Secret password) {
        try {
            // Created this mock url using https://beeceptor.com/
            URL url = new URL("https://onboarding.free.beeceptor.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String auth = userName + ":" + password.getPlainText();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return FormValidation.ok("Connection established successfully");
            } else {
                return FormValidation.warning("Failed! Server returned status code: " + responseCode);
            }
        } catch (Exception e) {
            return FormValidation.error("Client error: " + e.getMessage());
        }
    }

    public FormValidation doTestPayload(
            @QueryParameter("userName") String userName,
            @QueryParameter("password") Secret password,
            @QueryParameter("payload") Secret payload) {
        try {
            // Created this mock url using https://beeceptor.com/
            URL url = new URL("https://onboarding.free.beeceptor.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // Required for sending a body

            // 1. Basic Auth Header
            String auth = userName + ":" + password.getPlainText();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // 2. Set Content Type
            conn.setRequestProperty("Content-Type", "text/plain");

            // 3. Send the Payload Body
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getPlainText().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED) {
                return FormValidation.ok("Payload sent successfully!");
            } else {
                return FormValidation.warning("Server rejected payload. Status: " + code);
            }
        } catch (Exception e) {
            return FormValidation.error("Error: " + e.getMessage());
        }
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
            this.payload = Secret.fromString(block.getString("payload"));
        } else {
            this.connectionConfig = false;
            this.userName = null;
            this.password = null;
            this.payload = null;
        }
        save();
        return true;
    }

}