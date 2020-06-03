package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class ErrorCatchBuilder extends Builder implements SimpleBuildStep {

    private final String jenkinsIp;
    private final String projectName;
    private final String username;

    @DataBoundConstructor
    public ErrorCatchBuilder(String jenkinsIp, String projectName, String username) {
        this.jenkinsIp = jenkinsIp;
        this.projectName = projectName;
        this.username = username;
    }

    public String getSideeXConsoleText() {
        // curl http://jenkinsIp/job/username_projectName/lastBuild/consoleText | Select-Object -Expand Content
        String consoleUrl = jenkinsIp + "/job/" + username + "_" + projectName + "/lastBuild/consoleText";
        HttpURLConnection conn = null;
        StringBuilder contentBuilder = new StringBuilder();

        try {
          URL url = new URL(consoleUrl);
          conn = (HttpURLConnection) url.openConnection();
          conn.setReadTimeout(10000);
          conn.setConnectTimeout(15000);
          conn.setRequestMethod("GET");
          conn.connect();
          try (BufferedReader br = new BufferedReader(
              new InputStreamReader(conn.getInputStream(), "UTF-8"));) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append("\n");
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
          if (conn != null) {
            conn.disconnect();
          }
        }

        String SideeXConsole = contentBuilder.toString();
        SideeXConsole = SideeXConsole.substring(SideeXConsole.indexOf("INFO Executing"), SideeXConsole.indexOf("SideeX Runner finished."));
        return SideeXConsole;
    }

    public ArrayList<String> getCatchResult(String SideeXConsole) {
        ArrayList<String> resultArray = new ArrayList<>();
        String resultConsole = "";

        while(SideeXConsole.contains("CATCH")) {
            SideeXConsole = SideeXConsole.substring(SideeXConsole.indexOf("CATCH") + 5);
            resultConsole = SideeXConsole.substring(SideeXConsole.indexOf("echo") + 7);
            resultArray.add(resultConsole.substring(0, resultConsole.indexOf("|") - 1));
        }

        return resultArray;
    }

    public ArrayList<String> getErrorResult(String SideeXConsole) {
        ArrayList<String> resultArray = new ArrayList<>();

        while(SideeXConsole.contains("ERROR")) {
            SideeXConsole = SideeXConsole.substring(SideeXConsole.indexOf("ERROR") + 6);
            resultArray.add(SideeXConsole.substring(0, SideeXConsole.indexOf("\n")));
        }

        return resultArray;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Start catching...");
        String SideeXConsole = getSideeXConsoleText();
        if(SideeXConsole.contains("CATCH") || SideeXConsole.contains("ERROR")) {
          listener.getLogger().println("SideeX test failed with the reason below:");
          listener.getLogger().println("");
          for(String s : getCatchResult(SideeXConsole)) {
            listener.getLogger().println(s);
          }
    
          for(String s : getErrorResult(SideeXConsole)) {
            listener.getLogger().println(s);
          }
          listener.getLogger().println("");
          throw new IOException("SideeX Test Error");
        }
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "SideeX Error Catcher";
        }

    }

}
