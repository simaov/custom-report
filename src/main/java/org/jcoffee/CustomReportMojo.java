package org.jcoffee;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "custom-report", defaultPhase = LifecyclePhase.SITE)
public class CustomReportMojo extends AbstractMavenReport {

    private static final String MAVEN_EXEC_GROUP_ID = "org.codehaus.mojo";
    private static final String MAVEN_EXEC_ARTIFACT_ID = "exec-maven-plugin";
    private static final String MAVEN_EXEC_VERSION = "1.3.1";

    @Parameter(required = true)
    private String name;

    @Parameter
    private String description;

    @Parameter(required = true)
    private File reportFolder;

    @Parameter
    private Set<String> exclude = new HashSet<String>();

    @Parameter
    private GenerateReportCmd generateReport;

    @Component
    private MavenProject mavenProject;

    @Component
    private Renderer siteRenderer;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    private Plugin plugin;
    private File target;
    private MojoExecutor.ExecutionEnvironment pluginEnv;
    private static final String FILE_TYPE_PATTERN_STRING = "\\.(html|HTML)$";
    private static final Pattern FILE_TYPE_PATTERN = Pattern.compile(FILE_TYPE_PATTERN_STRING);


    @Override
    protected String getOutputDirectory() {
        String baseDir = mavenProject.getBasedir().toString();
        return baseDir.concat("/target/site/");
    }

    @Override
    protected MavenProject getProject() {
        return mavenProject;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        if (generateReport != null) {
            pluginEnv = executionEnvironment(mavenProject, mavenSession, pluginManager);
            plugin = plugin(MAVEN_EXEC_GROUP_ID, MAVEN_EXEC_ARTIFACT_ID, MAVEN_EXEC_VERSION);

            try {
                executeMojo(plugin, goal("exec"), this.createExecConfig(), pluginEnv);
            } catch (MojoExecutionException e) {
                getLog().error("Could not generate report. " + e.toString());
                return;
            }
        }

        this.createReport();

    }

    private void createReport() throws MavenReportException {
        if (reportFolder.exists() && reportFolder.isDirectory()) {

            this.copyReportDirectory();

            this.buildPage(this.getFileSet(), this.getSink());

        } else {
            getLog().warn("Report folder does not exists.");
        }
    }

    private void copyReportDirectory() throws MavenReportException {
        target = new File(this.getOutputDirectory().concat(reportFolder.getName()));
        try {
            getLog().info("Copy from " + reportFolder + " to " + target.getAbsolutePath());
            Files.walkFileTree(reportFolder.toPath(), new CopyDirectory(reportFolder.toPath(), target.toPath()));
        } catch (IOException e) {
            throw new MavenReportException("Can not copy report folder to " + target.getAbsolutePath(), e);
        }
    }

    private void buildPage(Set<String> fileList, Sink sink) {
        sink.head();
        sink.title();
        sink.text(name);
        sink.title_();
        sink.head_();
        sink.body();
        sink.section1();
        sink.sectionTitle1();
        sink.text(name);
        sink.sectionTitle1_();
        sink.section1_();
        sink.list();
        for (String s : fileList) {
            sink.listItem();
            sink.link(reportFolder.getName() + "/" + s);
            sink.text(s.replaceAll(FILE_TYPE_PATTERN_STRING, ""));
            sink.link_();
            sink.listItem_();
        }
        sink.link_();
        sink.body_();
        sink.flush();
        sink.close();
    }

    private Set<String> getFileSet() {
        Set<String> fileList = new HashSet<String>();
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FILE_TYPE_PATTERN.matcher(name).find();
            }
        };
        for (File f : target.listFiles(filenameFilter)) {
            if (!exclude.contains(f.getName())) {
                fileList.add(f.getName());
            }
        }
        return fileList;
    }

    private Xpp3Dom createExecConfig() throws MojoExecutionException{
        if (generateReport.getCommand() == null || generateReport.getCommand().isEmpty()) {
            throw new MojoExecutionException("Command cannot be NULL or empty.");
        }

        Xpp3Dom config = configuration(element(name("executable"), generateReport.getCommand()));
        if (generateReport.getArguments() != null) {
            List<Element> elements = new ArrayList<Element>(generateReport.getArguments().size());
            for (String arg : generateReport.getArguments()) {
                Element element = new Element(name("argument"), arg);
                elements.add(element);
            }
            Element[] e = new Element[elements.size()];
            config.addChild(element(name("arguments"), elements.toArray(e)).toDom());
        }
        return config;
    }

    @Override
    public String getOutputName() {
        return name.toLowerCase().replace(" ", "_");
    }

    @Override
    public String getName(Locale locale) {
        return name;
    }

    @Override
    public String getDescription(Locale locale) {
        return description == null ? name : description;
    }

    @Override
    public Renderer getSiteRenderer() {
        return siteRenderer;
    }
}
