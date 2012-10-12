package br.edu.ufcg.dsc.saferefactor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import refactoring.Constants;
import refactoring.FileUtil;
import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.saferefactor.core.Criteria;
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
	public static boolean isRefactoring(int idSource, int idTarget, String source, String target, String classes, int timeout, int maxTests, Approach approach, Criteria criteria, String sourceLpsPath, String targetLpsPath, ResultadoLPS resultado, boolean sourceIsCompiled, boolean targetIsCompiled) throws IOException {

		boolean isRefinement = true;

		FileUtil.createTestFolders();

		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");
		
		/* Central representation of an Ant project*/
		Project p = new Project();

		if (source.startsWith("/") && source.charAt(2) == ':') {
			source = source.substring(1, source.length());
		}

		if (target.startsWith("/") && target.charAt(2) == ':') {
			target = target.substring(1, target.length());
		}

		/* Set a user property, which cannot be overwritten by set/unset property calls. */
		p.setUserProperty(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.properties", buildFile.getAbsolutePath());

		if (timeout != 0) {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("timeout", String.valueOf(timeout));
		} else {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("timeout", "0");
		}

		if (maxTests != 0) {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("maxtests", String.valueOf(maxTests));
		} else {
			/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
			p.setProperty("maxtests", "0");
		}

		/*  Set a property. Any existing property of the same name is overwritten, unless it is a user property. */
		p.setProperty("source", source);
		p.setProperty("target", target);
		p.setProperty("lpsSource", sourceLpsPath);
		p.setProperty("lpsTarget", targetLpsPath);

		if (classes != null) {
			p.setProperty("classes", classes);
		}

		p.setProperty("tests.folder", Constants.TEST);
		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);
		p.setProperty("abordagem", approach.toString());
		p.setProperty("criteria", criteria.toString());

		String pathCobertura = null;

		p.setProperty("coverage_name", pathCobertura + "+" + approach);

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

		if (approach == Approach.ONLY_CHANGED_CLASSES) {
			/*Execute the specified target and any targets it depends on.*/
			p.executeTarget("clean_tests");
		} else {
			/*Execute the specified target and any targets it depends on.*/
			p.executeTarget("clean");
		}

		resultado.getMeasures().getTempoCompilacaoProdutos().startContinue();

		if (!targetIsCompiled) {
			p.executeTarget("compile_target_ind");
		}
		if (!sourceIsCompiled) {
			p.executeTarget("compile_source");
		}

		resultado.getMeasures().getTempoCompilacaoProdutos().pause();

		Saferefactor sr = new Saferefactor(source, target, "bin", "src", "lib", classes, maxTests, criteria);

		isRefinement = sr.isRefactoring(String.valueOf(timeout), false);

		if (isRefinement) {
			System.out.println("SafeRefactor found NO behavioral changes");
		} else {
			System.out.println("\n\n -- SafeRefactor FOUND behavioral changes -- ");
			System.out.println("\n Safe Refactor Tests' directory:" + "< " + Constants.TEST + " >");
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
	public static boolean isRefactoring(Product sourceProduct, Product targetProduct, String classes, int timeout, int maxTests, Approach approach, Criteria criteria, ResultadoLPS resultado) throws IOException {

		boolean result = false;

		String source = sourceProduct.getPath();
		String target = targetProduct.getPath();

		String sourceLPSPath = sourceProduct.getSpl().getPath();
		String targetLPSPath = targetProduct.getSpl().getPath();

		int idSource = sourceProduct.getId();
		int idTarget = targetProduct.getId();

		result = isRefactoring(idSource, idTarget, source, target, classes, timeout, maxTests, approach, criteria, sourceLPSPath, targetLPSPath, resultado, sourceProduct.isCompiled(), targetProduct.isCompiled());

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
