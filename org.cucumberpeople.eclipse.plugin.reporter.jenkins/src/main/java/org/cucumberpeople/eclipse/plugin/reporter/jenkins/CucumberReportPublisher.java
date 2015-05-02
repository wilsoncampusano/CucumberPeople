package org.cucumberpeople.eclipse.plugin.reporter.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.slaves.SlaveComputer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import org.apache.tools.ant.DirectoryScanner;
import org.cucumberpeople.eclipse.plugin.common.report.ReportBuilder;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;



@SuppressWarnings("unchecked")
public class CucumberReportPublisher extends Recorder {

    private final static String DEFAULT_FILE_INCLUDE_PATTERN = "**/*.json";
   // private final static String DEFAULT_FILE_INCLUDE_PATTERN_VIDEO = "**/*.*";

    public final String jsonReportDirectory;
    public final String copyrightName;
    public final String pluginUrlPath;
    public final String fileIncludePattern;
    public final String fileExcludePattern;
    public final boolean skippedFails;
    public final boolean undefinedFails;
    public final boolean noFlashCharts;
	public final boolean ignoreFailedTests;
    public final boolean parallelTesting;
  

    @DataBoundConstructor
    public CucumberReportPublisher(String jsonReportDirectory,String copyrightName, String pluginUrlPath, String fileIncludePattern, String fileExcludePattern, boolean skippedFails, boolean undefinedFails, boolean noFlashCharts, boolean ignoreFailedTests, boolean parallelTesting) {
        this.jsonReportDirectory = jsonReportDirectory;
        this.copyrightName=copyrightName;
        this.pluginUrlPath = pluginUrlPath;
        this.fileIncludePattern = fileIncludePattern;
        this.fileExcludePattern = fileExcludePattern;
        this.skippedFails = skippedFails;
        this.undefinedFails = undefinedFails;
        this.noFlashCharts = noFlashCharts;
		this.ignoreFailedTests = ignoreFailedTests;
        this.parallelTesting = parallelTesting;
    }

    private String[] findJsonFiles(File targetDirectory, String fileIncludePattern, String fileExcludePattern) {
        DirectoryScanner scanner = new DirectoryScanner();
        if (fileIncludePattern == null || fileIncludePattern.isEmpty()) {
            scanner.setIncludes(new String[]{DEFAULT_FILE_INCLUDE_PATTERN});
        } else {
            scanner.setIncludes(new String[]{fileIncludePattern});
        }
        scanner.setExcludes(new String[]{fileExcludePattern});
        scanner.setBasedir(targetDirectory);
        scanner.scan();
        return scanner.getIncludedFiles();
    }
    
    private String findFilePath(File targetDirectory, String fileIncludePattern, String fileExcludePattern) {
        DirectoryScanner scanner = new DirectoryScanner();
        if (fileIncludePattern == null || fileIncludePattern.isEmpty()) {
            scanner.setIncludes(new String[]{DEFAULT_FILE_INCLUDE_PATTERN});
        } else {
            scanner.setIncludes(new String[]{fileIncludePattern});
        }
        scanner.setExcludes(new String[]{fileExcludePattern});
        scanner.setBasedir(targetDirectory);
        scanner.scan();
        String[] includedFiles = scanner.getIncludedFiles();
        
      /*  List<String> fullPathList = new ArrayList<String>();
        for (String file : includedFiles) {
            fullPathList.add(new File(targetDirectory, file).getAbsolutePath());
        }*/
        if(includedFiles.length>0){
        return includedFiles[0];
        }else{
        	return null;
        }
        
    }
    
    private List<String> findAllFilePath(File targetDirectory, String fileIncludePattern, String fileExcludePattern) {
        DirectoryScanner scanner = new DirectoryScanner();
        if (fileIncludePattern == null || fileIncludePattern.isEmpty()) {
            scanner.setIncludes(new String[]{DEFAULT_FILE_INCLUDE_PATTERN});
        } else {
            scanner.setIncludes(new String[]{fileIncludePattern});
        }
        scanner.setExcludes(new String[]{fileExcludePattern});
        scanner.setBasedir(targetDirectory);
        scanner.scan();
        String[] includedFiles = scanner.getIncludedFiles();
        
        List<String> fullPathList = new ArrayList<String>();
        for (String file : includedFiles) {
            fullPathList.add(new File(targetDirectory, file).getAbsolutePath());
        }
        
        return fullPathList;
        
    }
    

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {

        listener.getLogger().println("[CucumberBuilder] Compiling Cucumber Html Reports ...");

        // source directory (possibly on slave)
        FilePath workspaceJsonReportDirectory;
        
        //the video folder (in slave host)
        FilePath videoReportDirectory;
        
       // String envFilePathTemp;
        
        if (jsonReportDirectory.isEmpty()) {
            workspaceJsonReportDirectory = build.getWorkspace();
        } else {
            workspaceJsonReportDirectory = new FilePath(build.getWorkspace(), jsonReportDirectory);
        }
        
        videoReportDirectory=new FilePath(build.getWorkspace(), "test-result");
        
     

        // target directory (always on master)
        File targetBuildDirectory = new File(build.getRootDir(), "cucumber-html-reports");
        if (!targetBuildDirectory.exists()) {
            targetBuildDirectory.mkdirs();
        }
        
        //target directory for replay file in master folder
    /*    File targetreplayDirectory = new File(build.getRootDir(), "cucumber-replay-reports");
        if (!targetreplayDirectory.exists()) {
        	targetreplayDirectory.mkdirs();
        }*/

        String buildNumber = Integer.toString(build.getNumber());
        String buildProject = build.getProject().getName();

        if (Computer.currentComputer() instanceof SlaveComputer) {
            listener.getLogger().println("[CucumberBuilder] copying all json files from slave: " + workspaceJsonReportDirectory.getRemote() + " to master reports directory: " + targetBuildDirectory);
        } else {
            listener.getLogger().println("[CucumberBuilder] copying all json files from: " + workspaceJsonReportDirectory.getRemote() + " to reports directory: " + targetBuildDirectory);
        }
        
        workspaceJsonReportDirectory.copyRecursiveTo(DEFAULT_FILE_INCLUDE_PATTERN, new FilePath(targetBuildDirectory));
        
        
        if (Computer.currentComputer() instanceof SlaveComputer) {
            listener.getLogger().println("[CucumberBuilder] copying all video and screenshot files from slave: " + videoReportDirectory.getRemote() + " to master reports directory: " + targetBuildDirectory);
        } else {
            listener.getLogger().println("[CucumberBuilder] copying all video and screenshot files from: " + videoReportDirectory.getRemote() + " to reports directory: " + targetBuildDirectory);
        }
        
       
        videoReportDirectory.copyRecursiveTo(new FilePath(targetBuildDirectory));

        // generate the reports from the targetBuildDirectory
		Result result = Result.NOT_BUILT;
        String[] jsonReportFiles = findJsonFiles(targetBuildDirectory, fileIncludePattern, fileExcludePattern);
        if (jsonReportFiles.length != 0) {

            listener.getLogger().println("[CucumberBuilder] Found the following number of json files: " + jsonReportFiles.length);
            int jsonIndex = 0;
            for (String jsonReportFile : jsonReportFiles) {
                listener.getLogger().println("[CucumberBuilder] " + jsonIndex + ". Found a json file: " + jsonReportFile);
                jsonIndex++;
            }
            listener.getLogger().println("[CucumberBuilder] Generating Cucumber HTML reports");

            
            String videofile=findFilePath(targetBuildDirectory,"**/*.mp4",fileExcludePattern);
          //  List<String> screenshotfile=findAllFilePath(targetBuildDirectory,"**/*.png",fileExcludePattern);
            
            
            try {      
            	//20150325 Alter: fix the parameter issue .
                ReportBuilder reportBuilder = new ReportBuilder(
                        fullPathToJsonFiles(jsonReportFiles, targetBuildDirectory),
                        copyrightName,
                        targetBuildDirectory,
                        pluginUrlPath,
                        buildProject,
                        buildNumber,
                        skippedFails,
                        undefinedFails,
                        !noFlashCharts,
                        true,
                        false,
                        "",
                        false);
                reportBuilder.generateReports(videofile);

				boolean buildSuccess = reportBuilder.getBuildStatus();

				if (buildSuccess)
				{
					result = Result.SUCCESS;
				}
				else
				{
					result = ignoreFailedTests ? Result.UNSTABLE : Result.FAILURE;
				}
				
				//set the global environment
				//new EnvironmentImpl(envFilePathTemp,listener);
				
				
				
            } catch (Exception e) {
                e.printStackTrace();
				result = Result.FAILURE;
                listener.getLogger().println("[CucumberBuilder] there was an error generating the reports: " + e);
                for(StackTraceElement error : e.getStackTrace()){
                   listener.getLogger().println(error);
                }
            }
        } else {
			result = Result.SUCCESS;
            listener.getLogger().println("[CucumberBuilder] there were no json results found in: " + targetBuildDirectory);
        }

        build.addAction(new CucumberReportBuildAction(build));
		build.setResult(result);
		
        return true;
    }

    private List<String> fullPathToJsonFiles(String[] jsonFiles, File targetBuildDirectory) {
        List<String> fullPathList = new ArrayList<String>();
        for (String file : jsonFiles) {
            fullPathList.add(new File(targetBuildDirectory, file).getAbsolutePath());
        }
        return fullPathList;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new CucumberReportProjectAction(project);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return "Publish Cucumber Test result as html report";
        }


        // Performs on-the-fly validation on the file mask wildcard.
        @SuppressWarnings("rawtypes")
		public FormValidation doCheck(@AncestorInPath AbstractProject project,
                                      @QueryParameter String value) throws IOException, ServletException {
            FilePath ws = project.getSomeWorkspace();
            return ws != null ? ws.validateRelativeDirectory(value) : FormValidation.ok();
        }

        @SuppressWarnings("rawtypes")
		@Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    private Properties readPropsFromFile(String path, Map<String, String> currentMap)
    {
        //console(Messages.EnvFileBuildWrapper_Console_ReadingFile());

        Properties props = new Properties();
        FileInputStream fis = null;
        String resolvedPath = Util.replaceMacro(path, currentMap);
       // console(Messages.EnvFileBuildWrapper_Console_PathToFile() + ": " + resolvedPath);

        try
        {
            if(path != null)
            {
                fis = new FileInputStream(resolvedPath);
                props.load(fis);
            }
            else
            {
               // console(Messages.EnvFileBuildWrapper_Console_EmptyPath());
            }
        }
        catch (FileNotFoundException e)
        {
           // console(Messages.EnvFileBuildWrapper_Console_FileNotFound() + " " +
           //         Messages.EnvFileBuildWrapper_Console_PathToFile() + "=[" + resolvedPath + "]");
           // logger.warning("Environment file not found. Path to file=[" + resolvedPath + "]");
        }
        catch (IOException e)
        {
            //console(Messages.EnvFileBuildWrapper_Console_UnableToReadFile());
           // logger.warning("Unable to read from environment file. " + e.getClass().getName());
        }
        finally
        {
            close(fis);
        }

        return props;
    }
    
    /**
     * Helper to close environment file.
     * @param fis {@link FileInputStream} for environment file.
     */
    private void close(FileInputStream fis)
    {
        try
        {
            if(fis != null)
            {
                fis.close();
            }
        }
        catch (Exception e)
        {
           // console(Messages.EnvFileBuildWrapper_Console_UnableToCloseFile());
           // logger.warning("Unable to close environment file.");
        }
    }
    
    class EnvironmentImpl extends Environment{

    	private String filePath;
    	private BuildListener listener;
    	public EnvironmentImpl(String filePath,BuildListener listener){
    		this.filePath=filePath;
    		this.listener=listener;
    	}
		@Override
		public void buildEnvVars(Map<String, String> env) {
			// TODO Auto-generated method stub
			//super.buildEnvVars(env);
			    Map<String, String> tmpFileEnvMap = new HashMap<String, String>();
	            Map<String, String> newFileEnvMap = new HashMap<String, String>();

	            tmpFileEnvMap.putAll(env);

	            //Fetch env variables from fil as properties
	            Properties envProps = readPropsFromFile(filePath, env);

	            if(envProps != null || envProps.size() < 1)
	            {

	                //Add env variables to temporary env map and file map containing new variables.
	                for (Entry<Object, Object> prop : envProps.entrySet())
	                {
	                    String key = prop.getKey().toString();
	                    String value = prop.getValue().toString();
	                    newFileEnvMap.put(key, value);
	                    tmpFileEnvMap.put(key, value);
	                }

	                // Resolve all variables against each other.
	                EnvVars.resolve(tmpFileEnvMap);

	                //Print resolved variables and copy resolved value to return map.
	                for(String key : newFileEnvMap.keySet())
	                {
	                    newFileEnvMap.put(key, tmpFileEnvMap.get(key));
	                    listener.getLogger().println("[CucumberBuilder] "+key + "=" + newFileEnvMap.get(key));
	                   // console(key + "=" + newFileEnvMap.get(key));
	                }

	            }
	            
			   env.putAll(newFileEnvMap);
		}
    	
    	
    }
}
