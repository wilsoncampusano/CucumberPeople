package org.cucumberpeople.eclipse.plugin.reporter.jenkins;

import hudson.FilePath;

import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public abstract class CucumberReportBaseAction implements Action {
    
    public String getUrlName(){
        return "cucumber-html-reports";
    }
    
    public String getDisplayName(){
        return "Cucumber Builder";
    }
    
    public String getIconFileName(){
            return "/plugin/cucumber-builder/cuke.png";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(), "graph.gif", false);
        dbs.setIndexFileName("feature-overview.html");
        dbs.generateResponse(req, rsp, this);
    }

    protected abstract String getTitle();

    protected abstract File dir();
}










