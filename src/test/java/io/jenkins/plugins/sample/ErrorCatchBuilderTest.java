package io.jenkins.plugins.sample;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ErrorCatchBuilderTest {
    // @Test
    // public void test() {
    //     String jenkinsIp = "";
    //     ErrorCatchBuilder errorCatchBuilder = new ErrorCatchBuilder(jenkinsIp);
    //     System.out.print(errorCatchBuilder.getCurrentConsoleText());
    // }

    public static void main(String[] args) {
    //   ErrorCatchBuilderTest main = new ErrorCatchBuilderTest();
    //   for(String s : main.getCatchResult(main.Builder("consoleText.txt"))){
    //       System.out.println(s);
    //   }

    //   for(String s : main.getErrorResult(main.Builder("consoleText.txt"))){
    //     System.out.println(s);
    //   }
    ErrorCatchBuilder errorCatchBuilder = new ErrorCatchBuilder("jenkinsIp", "projectName", "username");
    String s = errorCatchBuilder.getSideeXConsoleText();
    for(String s2 : errorCatchBuilder.getCatchResult(s)) {
        System.out.println(s2);
    }

    for(String s2 : errorCatchBuilder.getErrorResult(s)) {
        System.out.println(s2);
    }
    }

    public String Builder(String filePath) {
      StringBuilder contentBuilder = new StringBuilder();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
      {
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) 
        {
            contentBuilder.append(sCurrentLine).append("\n");
        }
      }  
      catch (IOException e) 
      {
        e.printStackTrace();
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

}