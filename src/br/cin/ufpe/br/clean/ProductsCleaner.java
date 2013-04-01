package br.cin.ufpe.br.clean;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import br.edu.ufcg.dsc.Constants;

public class ProductsCleaner {
	
	public ProductsCleaner() {
		super();
	}
	/**
	* This method cleans the generated products folder.
	*/
	public void cleanProductsFolder() {
		File buildFile = new File(br.edu.ufcg.dsc.Constants.BUILD_FILE_PATH);
		/*It creates a new ANT project.*/
		Project p = new Project();
		/*This is the directory of the generated products: */
		System.out.println("\nThe directory of the generated products: " + "<  Tool Path + Products  >\n");
		/* Set an ANT Build XML File property. Any existing property of the same name is overwritten, unless it is a user property.*/
		p.setProperty("productsFolder", Constants.PRODUCTS_DIR);
		/* Set an ANT Build XML File property. Any existing property of the same name is overwritten, unless it is a user property.*/
		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);
		/* Writes build events to a PrintStream. Currently, it only writes which targets are being executed, and any messages that get logged.*/
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);
		/*Initialise the ANT project.*/
		p.init();
		/* Configures a Project (complete with Targets and Tasks) based on a XML build file. It'll rely on a plugin to do the actual processing of the xml file.*/
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		/*Add a reference to the project.*/
		p.addReference("ant.projectHelper", helper);
		/*Parses the project file, configuring the project as it goes.*/
		helper.parse(p, buildFile);
		/*Execute the specified target and any targets it depends on.*/
		p.executeTarget("clean_products_folder");
		System.out.println("\n Two directories have been deleted:  < Tool Path + Products > and < pluginpath + emma + instr >");
	}
}
