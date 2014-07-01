package com.levelup;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mojo(name = "custom-report", defaultPhase = LifecyclePhase.SITE)
public class CustomReportMojo extends AbstractMavenReport {

    @Parameter(required = true)
    private String name;

    @Parameter
    private String description;

    @Parameter(required = true)
    private File reportFolder;

    @Parameter
    private Set<String> exclude = new HashSet<String>();

    @Component
    private MavenProject mavenProject;

    @Component
    private Renderer siteRenderer;

    private File target;
    private final static String FILE_TYPE = ".html";


    @Override
    protected String getOutputDirectory() {
        return "target/site/";
    }

    @Override
    protected MavenProject getProject() {
        return  mavenProject;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        if (reportFolder.exists() && reportFolder.isDirectory()) {
            target = new File(getOutputDirectory().concat(reportFolder.getName()));
            try {
                getLog().info("Copy from " + reportFolder + ", to " + target.getAbsolutePath());
                Files.walkFileTree(reportFolder.toPath(), new CopyDirectory(reportFolder.toPath(), target.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new MavenReportException("Can not copy report folder to " + target.getAbsolutePath(), e);
            }

            this.buildPage(this.getFileSet(), this.getSink());

        } else {
            throw new MavenReportException("Report folder does not exists.");
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
        for (String s : fileList){
            sink.listItem();
            sink.link(reportFolder.getName() + "/" + s);
            sink.text(s.replace(FILE_TYPE, ""));
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
                return name.toLowerCase().endsWith(FILE_TYPE);
            }
        };
        for (File f : target.listFiles(filenameFilter)) {
            if (!exclude.contains(f.getName())) {
                fileList.add(f.getName());
            }
        }
        return fileList;
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
