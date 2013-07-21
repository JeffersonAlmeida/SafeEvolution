package br.edu.ufcg.dsc.saferefactor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import refactoring.Constants;
import refactoring.FileUtil;
import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.saferefactor.core.Analyzer;
import br.edu.ufcg.saferefactor.core.Saferefactor;

public class CommandLine {

	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/safeRefactor";

	public static Report checkRefactoring(String source, String target, String timeout) {
		return null;
	}

	/**
	 * 
	 * @param idSource
	 * @param idTarget
	 * @param source
	 * @param target
	 * @param classes
	 * @param timeout
	 * @param maxTests
	 * @param approach
	 * @param criteria
	 * @param sourceLpsPath
	 * @param targetLpsPath
	 * @param resultado
	 * @param sourceIsCompiled
	 * @param targetIsCompiled
	 * @return
	 * @throws IOException
	 */
	// int idSource, int idTarget, String source, String target, String classes, int timeout, int maxTests, Approach approach, Criteria criteria, String sourceLpsPath, String targetLpsPath, boolean sour
	public static boolean isRefactoring(int sourceProductId, int targetProductId, String sourceProductPath, String targetProductPath, Collection<String> impactedClasses, FilePropertiesObject propertiesObject, boolean sourceIsCompiled, boolean targetIsCompiled) throws IOException {

		boolean isRefinement = true;

		FileUtil.createTestFolders();

		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");
		
		/* Central representation of an Ant project*/
		Project p = new Project();

		if (sourceProductPath.startsWith("/") && sourceProductPath.charAt(2) == ':') {
			sourceProductPath = sourceProductPath.substring(1, sourceProductPath.length());
		}

		if (targetProductPath.startsWith("/") && targetProductPath.charAt(2) == ':') {
			targetProductPath = targetProductPath.substring(1, targetProductPath.length());
		}

		/* Set a user property, which cannot be overwritten by set/unset property calls. */
		p.setUserProperty(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.properties", buildFile.getAbsolutePath());

		if (propertiesObject.getTimeOut() != 0) {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("timeout", String.valueOf(propertiesObject.getTimeOut()));
		} else {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("timeout", "60");
		}

		if (propertiesObject.getInputLimit() != 0) {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("maxtests", String.valueOf(propertiesObject.getInputLimit()));
		} else {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("maxtests", "60");
		}

		/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
		p.setProperty("source", sourceProductPath);
		p.setProperty("target", targetProductPath);
		p.setProperty("lpsSource", propertiesObject.getSourceLineDirectory());
		p.setProperty("lpsTarget", propertiesObject.getTargetLineDirectory());

		p.setProperty("tests.folder", Constants.TEST);
		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);
		p.setProperty("abordagem", propertiesObject.getApproach().toString());
		p.setProperty("criteria", propertiesObject.getWhichMethods().toString());

		String pathCobertura = null;

		p.setProperty("coverage_name", pathCobertura + "+" + propertiesObject.getApproach());

		/* Writes build events to a PrintStream. Currently, it only writes which targets are being executed, and any messages that get logged. */
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out); 
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		/*  Initialise the project. */
		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		
		/*  Initialise the project. */	
		helper.parse(p, buildFile);

		if (propertiesObject.getApproach() == Approach.IC) {
			/*Execute the specified target and any targets it depends on.*/
			p.executeTarget("clean_tests");
		} else {
			/*Execute the specified target and any targets it depends on.*/
			p.executeTarget("clean");
		}

		SPLOutcomes.getInstance().getMeasures().getTempoCompilacaoProdutos().startContinue();

		/*if (!targetIsCompiled) {
			p.executeTarget("compile_target_ind");
		}
		if (!sourceIsCompiled) {
			p.executeTarget("compile_source");
		}*/

		SPLOutcomes.getInstance().getMeasures().getTempoCompilacaoProdutos().pause();

		org.sr.input.FilePropertiesObject input = new org.sr.input.FilePropertiesObject();
		input.setSourceLineDirectory(sourceProductPath + Constants.FILE_SEPARATOR);
		input.setTargetLineDirectory(targetProductPath + Constants.FILE_SEPARATOR);
		input.setTimeOut(propertiesObject.getTimeOut());
		input.setInputLimit(propertiesObject.getInputLimit());
		input.setGenerateTestsWith(propertiesObject.getGenerateTestsWith());
		input.setWhichMethods(propertiesObject.getWhichMethods());
		Saferefactor safeRefactor = new Saferefactor(impactedClasses, input);
		//Saferefactor sr = new Saferefactor(sourceProductPath, targetProductPath, "bin", "src", "lib", classes, propertiesObject.getInputLimit(), propertiesObject.getWhichMethods());

		Analyzer analyzer = new Analyzer();
		analyzer.setInput(safeRefactor.getInput());
		safeRefactor.setAnalyzer(analyzer);
		safeRefactor.getAnalyzer().setImpactedClasses(safeRefactor.getIc());
		
		isRefinement = safeRefactor.isRefactoring(String.valueOf(input.getTimeOut()), true, input.getGenerateTestsWith());

		  if(input.getGenerateTestsWith().equals("evosuite")){ // copy evosuite report to /tmp^M
              br.edu.ufcg.saferefactor.core.util.FileUtil.copyFromTo(new File(input.getSourceLineDirectory() + "src" + "/evosuite-report"), new File("/tmp/"+ propertiesObject.getApproach() + "/evosuite-report"));
      }else if(input.getGenerateTestsWith().equals("randoop")){ // copy randoop file to /tmp^M
              br.edu.ufcg.saferefactor.core.util.FileUtil.copyFromTo(new File(input.getSourceLineDirectory() + "/methods-to-test-list"), new File("/tmp/"+  propertiesObject.getApproach() + "/methods-to-test-list"));
              br.edu.ufcg.saferefactor.core.util.FileUtil.copyFromTo(new File(input.getSourceLineDirectory() + "src" + "/randoop"), new File("/tmp/"+  propertiesObject.getApproach() + "/randoop"));
      }
		
		if (isRefinement) {
			System.out.println("SafeRefactor found NO behavioral changes");
		} else {
			System.out.println("\n\n -- SafeRefactor FOUND behavioral changes -- ");
		}

		return isRefinement;
	}

	/**
	 * 
	 * @param sourceProduct
	 * @param targetProduct
	 * @param classes
	 * @param timeout
	 * @param maxTests
	 * @param approach
	 * @param criteria
	 * @param resultado
	 * @return
	 * @throws IOException
	 */
	//Product sourceProduct, Product targetProduct, String classes, int timeout, int maxTests, Approach approach, Criteria criteria, ResultadoLPS resultado
	public static boolean isRefactoring(Product sourceProduct, Product targetProduct, Collection<String> modifiedClasses, FilePropertiesObject propertiesObject) throws IOException {

		boolean result = false;

		String sourceProductPath = sourceProduct.getPath();
		String targetProductPath = targetProduct.getPath();

		int sourceProductId = sourceProduct.getId();
		int targetProductId = targetProduct.getId();

		// idSource, idTarget, source, target, classes, timeout, maxTests, approach, criteria, sourceLPSPath, targetLPSPath, sourceProduct.isCompiled(), targetProduct.isCompiled()
		result = isRefactoring(sourceProductId, targetProductId, sourceProductPath, targetProductPath, modifiedClasses, propertiesObject, sourceProduct.isCompiled(), targetProduct.isCompiled());

		sourceProduct.setCompiled(true);
		targetProduct.setCompiled(true);

		return result;
	}

	private static Properties loadTestProperties() throws IOException {
		Properties properties = new Properties();

		File fileProperties = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "ant" + Constants.FILE_SEPARATOR
				+ "tests.properties");

		if (fileProperties.exists()) {
			FileInputStream inputStream = new FileInputStream(fileProperties);

			properties.load(inputStream);

			inputStream.close();
		}

		return properties;
	}
}
