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

	public static boolean isRefactoring(int idSource, int idTarget, String source, String target, String classes, int timeout,
			int maxTests, Approach approach, Criteria criteria, String sourceLpsPath, String targetLpsPath, ResultadoLPS resultado,
			boolean sourceIsCompiled, boolean targetIsCompiled) throws IOException {

		boolean isRefinement = true;

		FileUtil.createTestFolders();

		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");
		Project p = new Project();
		// p.setUserProperty("ant.file", buildFile.getAbsolutePath());

		if (source.startsWith("/") && source.charAt(2) == ':') {
			source = source.substring(1, source.length());
		}

		if (target.startsWith("/") && target.charAt(2) == ':') {
			target = target.substring(1, target.length());
		}

		p.setUserProperty(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.properties", buildFile.getAbsolutePath());

		if (timeout != 0) {
			p.setProperty("timeout", String.valueOf(timeout));
		} else {
			p.setProperty("timeout", "0");
		}

		if (maxTests != 0) {
			p.setProperty("maxtests", String.valueOf(maxTests));
		} else {
			p.setProperty("maxtests", "0");
		}

		p.setProperty("source", source);
		p.setProperty("target", target);
		p.setProperty("lpsSource", sourceLpsPath);
		p.setProperty("lpsTarget", targetLpsPath);

		if (classes != null) {
			p.setProperty("classes", classes);
		}

		p.setProperty("tests.folder", Constants.TEST);
		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);
		// p.setProperty("filelog",
		// (Measures.getInstance().getFilePropertiesName() == null) ? "noname" :
		// Measures.getInstance().getFilePropertiesName());
		p.setProperty("abordagem", approach.toString());
		p.setProperty("criteria", criteria.toString());

		// String filePropertiesName =
		// Measures.getInstance().getFilePropertiesName();
		String pathCobertura = null;

		// if(filePropertiesName != null){
		// pathCobertura =
		// filePropertiesName.replaceFirst(Pattern.quote(".properties"),
		// "").split(Pattern.quote(Constants.FILE_SEPARATOR + "properties" +
		// Constants.FILE_SEPARATOR))[1];
		// }
		// else{
		// pathCobertura = "noname";
		// }

		p.setProperty("coverage_name", pathCobertura + "+" + approach);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		if (approach == Approach.ONLY_CHANGED_CLASSES) {
			p.executeTarget("clean_tests");
		} else {
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
			System.out.println("SafeRefactor found no behavioral changes");
		} else {
			System.out.println("SafeRefactor found behavioral changes");
			System.out.println("Tests' dir:" + Constants.TEST);
		}

		// p.executeTarget("change");
		//		
		// resultado.getMeasures().getTempoCompilacaoTestes().startContinue();
		// p.executeTarget("compile_tests");
		// resultado.getMeasures().getTempoCompilacaoTestes().pause();
		//		
		// p.executeTarget("instrument");
		//		
		// resultado.getMeasures().getTempoExecucaoTestes().startContinue();
		// p.executeTarget("run_tests_source");
		// resultado.getMeasures().getTempoExecucaoTestes().pause();
		//		
		// resultado.getMeasures().getTempoExecucaoTestes().startContinue();
		// p.executeTarget("run_tests_target");
		// resultado.getMeasures().getTempoExecucaoTestes().pause();
		//		
		// p.executeTarget("reporthtml");
		//
		// isRefinement = ResultComparator.hasSameBehavior(Constants.TESTSRC,
		// Constants.TESTTGT);
		//		
		// Properties testProterties = loadTestProperties();
		//		
		// String quantidadeTestes =
		// testProterties.getProperty("QUANTIDADE_TESTES");
		// if(quantidadeTestes != null && !quantidadeTestes.equals("")){
		// resultado.getMeasures().setQuatidadeTotalTestes(resultado.getMeasures().getQuatidadeTotalTestes()
		// + Integer.parseInt(quantidadeTestes));
		// }
		//		
		//		
		// if(!isRefinement){
		// Diferenca diferenca = new Diferenca();
		//			
		// if((idSource==idTarget || idSource==idTarget - 1) &&
		// method==Method.ALL_PUBLIC_METHODS){
		// String temMetodosRemovidosString =
		// testProterties.getProperty("METODOS_REMOVIDOS");
		//				
		// boolean temMetodosRemovidos =
		// Boolean.parseBoolean(temMetodosRemovidosString);
		//				
		// diferenca.setProdutoTemMesmosMetodosQuandoComparadoAoProdutoComMesmaConfiguracao(temMetodosRemovidos);
		//				
		// if(temMetodosRemovidos){
		// Collection<String> metodosRemovidos = new ArrayList<String>();
		// Collection<String> metodosAdicionados = new ArrayList<String>();
		//					
		// String metodosRemovidosString =
		// testProterties.getProperty("LISTA_METODOS_REMOVIDOS");
		// String metodosAdicionadosString =
		// testProterties.getProperty("LISTA_METODOS_ADICIONADOS");
		//					
		// String[] metodosRemovidosParts = metodosRemovidosString.split(",");
		// String[] metodosAdicioandosParts =
		// metodosAdicionadosString.split(",");
		//					
		// for(String part : metodosRemovidosParts){
		// if(!part.equals("")){
		// metodosRemovidos.add(part);
		// }
		// }
		//					
		// for(String part : metodosAdicioandosParts){
		// if(!part.equals("")){
		// metodosAdicionados.add(part);
		// }
		// }
		//					
		// diferenca.setMetotodosRemovidos(metodosRemovidos);
		// diferenca.setMetodosAdicionados(metodosAdicionados);
		// }
		// }
		//			
		//			
		// resultado.addDiferenca(diferenca);
		// }
		//		
		// File fileProperties = new File(IdentifyChange.PROPERTIES_FILE_PATH);
		//		
		// if(fileProperties.exists()){
		// fileProperties.delete();
		// }

		// Recuperar aqui métodos com comportamento diferente caso o resultado
		// seja false.

		// Apagar arquivo

		return isRefinement;
	}

	public static boolean isRefactoring(Product sourceProduct, Product targetProduct, String classes, int timeout, int maxTests,
			Approach approach, Criteria criteria, ResultadoLPS resultado) throws IOException {

		boolean result = false;

		String source = sourceProduct.getPath();
		String target = targetProduct.getPath();

		String sourceLPSPath = sourceProduct.getSpl().getPath();
		String targetLPSPath = targetProduct.getSpl().getPath();

		int idSource = sourceProduct.getId();
		int idTarget = targetProduct.getId();

		result = isRefactoring(idSource, idTarget, source, target, classes, timeout, maxTests, approach, criteria, sourceLPSPath,
				targetLPSPath, resultado, sourceProduct.isCompiled(), targetProduct.isCompiled());

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

	// public static SafeRefactorResult isRefactoringCompiledSource(String
	// source,
	// String target, String classes, int timeout, int maxTests, Approach feat)
	// {
	// // FileUtil.createTestFolders();
	// boolean result = true;
	//		
	// File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH
	// + "/ant/build.xml");
	// Project p = new Project();
	// // p.setUserProperty("ant.file", buildFile.getAbsolutePath());
	//
	// if(source.startsWith("/") && source.charAt(2) == ':'){
	// source = source.substring(1, source.length());
	// }
	//
	// if(target.startsWith("/") && target.charAt(2) == ':'){
	// target = target.substring(1, target.length());
	// }
	//
	// p.setProperty("source", source);
	// p.setUserProperty(br.edu.ufcg.dsc.Constants.PLUGIN_PATH
	// + "/ant/build.properties", buildFile.getAbsolutePath());
	// p.setProperty("target", target);
	//		
	// if (timeout != 0)
	// p.setProperty("timeout", String.valueOf(timeout));
	// else
	// p.setProperty("timeout", "0");
	//		
	// p.setProperty("classes", classes);
	//
	// if (maxTests != 0)
	// p.setProperty("maxtests", String.valueOf(maxTests));
	// else
	// p.setProperty("maxtests", "0");
	//
	//
	// p.setProperty("tests.folder", Constants.TEST);
	//
	// p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);
	//		
	// p.setProperty("filelog", Measures.getInstance().getFilePropertiesName());
	//		
	// p.setProperty("abordagem", feat.toString());
	//		
	//		
	// String filePropertiesName =
	// Measures.getInstance().getFilePropertiesName();
	// String pathCobertura = null;
	//		
	// if(filePropertiesName != null){
	// pathCobertura =
	// filePropertiesName.replaceFirst(Pattern.quote(".properties"),
	// "").split(Pattern.quote(Constants.FILE_SEPARATOR + "properties" +
	// Constants.FILE_SEPARATOR))[1];
	// }
	// else{
	// pathCobertura = "noname";
	// }
	//		
	//		
	// p.setProperty("coverage_name", pathCobertura + "+" + feat);
	//
	// DefaultLogger consoleLogger = new DefaultLogger();
	// consoleLogger.setErrorPrintStream(System.err);
	// consoleLogger.setOutputPrintStream(System.out);
	// consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
	// p.addBuildListener(consoleLogger);
	//
	// p.init();
	// ProjectHelper helper = ProjectHelper.getProjectHelper();
	// p.addReference("ant.projectHelper", helper);
	// helper.parse(p, buildFile);
	//		
	// // p.executeTarget("clean");
	//		
	// Measures.getInstance().getTempoCompilacaoProdutos().startContinue();
	// p.executeTarget("compile_target");
	// Measures.getInstance().getTempoCompilacaoProdutos().pause();
	//		
	//
	// // p.executeTarget("change");
	//		
	// long startedCompile = System.currentTimeMillis();
	//		
	// // Measures.getInstance().getTempoCompilacaoTestes().startContinue();
	// // p.executeTarget("compile_tests");
	// // Measures.getInstance().getTempoCompilacaoTestes().pause();
	//		
	// long finishedCompile = System.currentTimeMillis();
	//
	// long startedTesting = System.currentTimeMillis();
	//		
	// p.executeTarget("instrument");
	//		
	// Measures.getInstance().getTempoExecucaoTestes().startContinue();
	// p.executeTarget("run_tests");
	// Measures.getInstance().getTempoExecucaoTestes().pause();
	//		
	// p.executeTarget("reporthtml");
	//		
	// long finishedTesting = System.currentTimeMillis();
	//
	// result = ResultComparator.hasSameBehavior(Constants.TESTSRC,
	// Constants.TESTTGT);
	//
	// SafeRefactorResult output = new SafeRefactorResult(startedCompile,
	// finishedCompile, startedTesting, finishedTesting, result);
	// return output;
	//		
	//		
	//		
	// }
}
