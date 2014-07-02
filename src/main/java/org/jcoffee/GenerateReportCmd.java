package org.jcoffee;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Created by sima on 7/1/14.
 */
public class GenerateReportCmd {
    @Parameter
    private String command;
    @Parameter
    private List<String> arguments;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}
