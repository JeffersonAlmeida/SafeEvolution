package br.edu.ufcg.dsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.naming.ConfigurationException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.jdt.core.JavaModelException;
import soot.Main;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.ast.ASTComparator;
import br.edu.ufcg.dsc.builders.MobileMediaBuilder;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.builders.TargetBuilder;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.ck.ConfigurationItem;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.alloy.SafeCompositionVerifier;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.fm.AlloyFMEvolutionBuilder;
import br.edu.ufcg.dsc.fm.FeatureModelReader;
import br.edu.ufcg.dsc.fm.FeatureModelRefactVerifier;
import br.edu.ufcg.dsc.refactoringresults.FeatureModelEvolutionResult;
import br.edu.ufcg.dsc.refactoringresults.SafeCompositionResult;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.Comparador;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;
import br.edu.ufcg.saferefactor.core.Criteria;
import edu.mit.csail.sdg.alloy4.Err;

public class ToolCommandLine {

	private static final int MAX_CLASSES_MODIFICADAS = 1000;

	private String sourceFMSemantics;
	private String targetFMSemantics;
	private FilesManager filesManager;
	private HashSet<String> dependenciasCopiadas;

	private Collection<String> classesModificaadas;
	private ProductBuilder builder;
	private long testsCompileTimeout;
	private long testsExecutionTimeout;
	private long testsGenerationTimeout;
	private HashSet<String> changedAssets;

	private ASTComparator astComparator;

	private HashMap<String, HashSet<HashSet<String>>> cacheProducts;

	private HashSet<String> constantsPreProcessor;
	private HashSet<String> listaAspectos;

	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosSource;
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosTarget;

	private Lines line;

	public ToolCommandLine() {
		this.filesManager = FilesManager.getInstance();
		this.cacheProducts = new HashMap<String, HashSet<HashSet<String>>>();
		this.astComparator = new ASTComparator();
		try {
			this.astComparator.setUpProject();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public ToolCommandLine(Lines line) {
		this();

		this.line = line;

		if (line.equals(Lines.MOBILE_MEDIA) || line.equals(Lines.DEFAULT)) {
			this.builder = new MobileMediaBuilder();
		} else if (line.equals(Lines.TARGET)) {
			this.builder = new TargetBuilder();
		}
	}

	public boolean verifyLine(ProductLine souceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach,
			Criteria criteria, ResultadoLPS resultado) throws Err, IOException, AssetNotFoundException, DirectoryException {

		boolean isRefinement = false;

		this.setup(souceLine, targetLine);

		this.putProductsInCache(souceLine, targetLine);

		resultado.getMeasures().reset();
		resultado.getMeasures().setApproach(approach);
		resultado.getMeasures().getTempoTotal().startContinue();

		isRefinement = this.checkLPS(souceLine, targetLine, timeout, qtdTestes, approach, criteria, resultado);

		resultado.getMeasures().getTempoTotal().pause();
		resultado.getMeasures().print();

		return isRefinement;
	}

	private void putProductsInCache(ProductLine sourceLine, ProductLine targetLine) {
		if (this.cacheProducts.get(sourceLine.getPath()) == null || this.cacheProducts.get(targetLine.getPath()) == null) {
			buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
			buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);

			buildFMEvolutionAlloyFile(sourceLine.getFmPath(), targetLine.getFmPath());

			if (this.cacheProducts.get(sourceLine.getPath()) == null) {
				HashSet<HashSet<String>> productsSource = this.builder.getProductsFromAlloy(Constants.ALLOY_PATH
						+ Constants.SOURCE_FM_ALLOY_NAME);

				this.cacheProducts.put(sourceLine.getPath(), productsSource);
			}

			if (this.cacheProducts.get(targetLine.getPath()) == null) {
				HashSet<HashSet<String>> productsTarget = this.builder.getProductsFromAlloy(Constants.ALLOY_PATH
						+ Constants.TARGET_FM_ALLOY_NAME);

				this.cacheProducts.put(targetLine.getPath(), productsTarget);
			}

			HashSet<HashSet<String>> products = new HashSet<HashSet<String>>();

			HashSet<String> p1 = new HashSet<String>();
			p1.add("uceditor");
			p1.add("motorola");
			p1.add("windows");
			p1.add("ptbr");
			p1.add("environment");
			p1.add("importtemplate");
			p1.add("input");
			p1.add("basicgeneration");
			p1.add("xmloutput");
			p1.add("branding");
			p1.add("target");
			p1.add("language");
			p1.add("output");
			p1.add("testCaseGenerator");
			p1.add("xlsstdinput");
			p1.add("cm");

			//		products.add(p1);
			//		this.cacheProducts.put(sourcePath, products);
			//		this.cacheProducts.put(targetPath, products);
		}

		sourceLine.setSetsOfFeatures(this.cacheProducts.get(sourceLine.getPath()));
		targetLine.setSetsOfFeatures(this.cacheProducts.get(targetLine.getPath()));
	}

	private void setup(ProductLine souceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
		this.sourceFMSemantics = null;
		this.targetFMSemantics = null;

		this.dependenciasCopiadas = null;

		this.classesModificaadas = null;

		this.testsCompileTimeout = 0;
		this.testsExecutionTimeout = 0;
		this.testsGenerationTimeout = 0;
		this.changedAssets = null;

		XMLReader.getInstance().reset();

		this.cleanProducts();

		souceLine.setup();
		targetLine.setup();
	}

	private void cleanProducts() {
		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");

		Project p = new Project();
		System.out.println(" Constants.PRODUCTS_DIR = " + Constants.PRODUCTS_DIR);
		p.setProperty("productsFolder", Constants.PRODUCTS_DIR);

		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		p.executeTarget("clean_products_folder");
		System.out.println("OK");
	}

	public boolean checkLPS(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach,
			Criteria criteria, ResultadoLPS resultado) throws IOException, AssetNotFoundException, DirectoryException {

		boolean isRefinement = false;

		//Checa WF
		boolean isWF = this.isWF(sourceLine, targetLine);
		resultado.setWF(isWF);

		if (isWF) {
			//Checa FM
			//FM nao eh checado na Naive, apenas WF.

			boolean isFMAndCKRefinement = this.isFeatureModelAndConfigurationKnowledgeWeakRefinement(sourceLine, targetLine);
			resultado.setFMAndCKRefinement(isFMAndCKRefinement);

			if (approach == Approach.NAIVE_2_ICTAC || approach == Approach.NAIVE_1_APROXIMACAO || isFMAndCKRefinement) {

				//Checa se AM eh igual, exceto para Naive.
				boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceLine, targetLine);
				resultado.setAssetMappingsEqual(isAssetMappingsEqual);

				if (approach == Approach.NAIVE_2_ICTAC || approach == Approach.NAIVE_1_APROXIMACAO || !isAssetMappingsEqual) {

					HashSet<String> changedFeatures = null;

					if (approach == Approach.IMPACTED_FEATURES || approach == Approach.ONLY_CHANGED_CLASSES) {
						//Gera testes apenas para features impactadas.
						changedFeatures = getChangedFeatureNames(targetLine);
					}

					resultado.getMeasures().getTempoExecucaoAbordagem().startContinue();

					if (approach == Approach.ONLY_CHANGED_CLASSES) {
						//Gera testes apenas para classes modificadas e nao gera produtos da linha.
						//Gera duas vezes a quantidade de testes por metodo.
						isRefinement = this.isAssetMappingRefinement(sourceLine, targetLine, timeout, qtdTestes, approach, changedFeatures,
								criteria, resultado);
					} else {
						//Gera testes para todas as classes e produtos da linha.
						isRefinement = this.testProducts(sourceLine, targetLine, timeout, qtdTestes, approach, criteria, resultado);
					}

					resultado.getMeasures().getTempoExecucaoAbordagem().pause();

					resultado.getMeasures().setResult(isRefinement);

					if (isRefinement) {
						System.out.println("A LPS foi refinada!");
					} else {
						System.out.println("Nao eh refinamento. AM nao foi refinado.");
					}
				} else {
					resultado.getMeasures().setResult(true);

					System.out.println("A LPS foi refinada!");
				}
			} else {
				System.out.println("A tecnica " + approach + " nao pode ser aplicada. F' e K' nao refinam F e K.");
			}
		} else {
			System.out.println("Nao eh refinamento. Nao eh WF.");
		}

		return isRefinement;
	}

	@SuppressWarnings("unchecked")
	private boolean isFeatureModelAndConfigurationKnowledgeWeakRefinement(ProductLine sourceLine, ProductLine targetLine)
			throws IOException, AssetNotFoundException {
		boolean isRefinement = true;

		HashSet<HashSet<String>> setsOfFeaturesSource = sourceLine.getSetsOfFeatures();
		HashSet<HashSet<String>> setsOfFeaturesTarget = targetLine.getSetsOfFeatures();

		setsOfFeaturesTarget = (HashSet<HashSet<String>>) setsOfFeaturesTarget.clone();

		int id = 0;

		for (HashSet<String> featureSetSource : setsOfFeaturesSource) {
			Product productSource = this.evaluateProductCKAM(featureSetSource, sourceLine, id++);

			sourceLine.getProducts().add(productSource);

			if (setsOfFeaturesTarget.contains(featureSetSource)) {
				//Tentativa de encontrar um correspondente no target de forma mais economica.
				Product provavelCorrespondenteNoTarget = this.evaluateProductCKAM(featureSetSource, targetLine, id++);

				if (provavelCorrespondenteNoTarget.temMesmosAssetsEPreProcessConstants(productSource)) {
					productSource.setLikelyCorrespondingProduct(provavelCorrespondenteNoTarget);
				}

				targetLine.getProducts().add(provavelCorrespondenteNoTarget);

				//Remove do Target o produto jah criado. No final sobrabrarao no target
				//somente as configuracoes que nao existiam na linha original.
				setsOfFeaturesTarget.remove(featureSetSource);
			}
		}

		//Geracao de produtos que nao existiam na linha original. 
		//Na Naive, algum produto antigo pode passar a corresponder 
		//a este produto.
		for (HashSet<String> featureSetTarget : setsOfFeaturesTarget) {
			Product productTarget = this.evaluateProductCKAM(featureSetTarget, targetLine, id++);

			targetLine.getProducts().add(productTarget);
		}

		//Tentativa de casar os produtos que ainda nao tem par.
		for (Product productSource : sourceLine.getProducts()) {
			if (productSource.getLikelyCorrespondingProduct() == null) {
				Product provavelCorrespondenteNoTarget = this.getProvavelCorrespondenteNoTarget(productSource, targetLine.getProducts());

				if (provavelCorrespondenteNoTarget != null) {
					productSource.setLikelyCorrespondingProduct(provavelCorrespondenteNoTarget);
				} else {
					isRefinement = false;
				}
			}
		}

		return isRefinement;
	}

	private Product getProvavelCorrespondenteNoTarget(Product productSource, ArrayList<Product> productsTarget) {
		Product result = null;

		for (Product productTarget : productsTarget) {
			if (productTarget.temMesmosAssetsEPreProcessConstants(productSource)) {
				result = productTarget;

				break;
			}
		}

		return result;
	}

	private Product evaluateProductCKAM(HashSet<String> featureSet, ProductLine productLine, int id) throws IOException,
			AssetNotFoundException {
		//Constante -> Destino
		HashMap<String, String> constantesDestinos = productLine.getCk().evalCKDestinos(featureSet);

		//Constante -> Origem
		HashMap<String, String> constantesOrigens = new HashMap<String, String>();

		for (String constant : constantesDestinos.keySet()) {
			//Substituindo os nulos dos destinos, casos em que origem e destino sao iguais.
			constantesDestinos.put(constant, constantesDestinos.get(constant) == null ? productLine.getAssetMapping().get(constant.trim())
					: constantesDestinos.get(constant));

			//A origem sempre eh o path informado no AM.
			constantesOrigens.put(constant, productLine.getAssetMapping().get(constant.trim()));
		}

		HashSet<String> preProcessTags = this.builder.getPreProcessTags(featureSet);

		return new Product(productLine, id, featureSet, preProcessTags, constantesOrigens, constantesDestinos);
	}

	private boolean isWF(ProductLine sourceLine, ProductLine targetLine) {
		this.buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
		this.buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);

		this.buildAlloyCKFile(Constants.SOURCE_CK_ALLOY_NAME, this.sourceFMSemantics, "source", sourceLine);
		this.buildAlloyCKFile(Constants.TARGET_CK_ALLOY_NAME, this.targetFMSemantics, "target", targetLine);

		// Checando se ambas sao bem formadas
		SafeCompositionResult sourceComposition = checkSafeCompositionOfLine(Constants.SOURCE_CK_ALLOY_NAME, sourceLine.getFeatures(),
				"source");

		System.out.println("RESULTADO DO WF SOURCE: " + !sourceComposition.getAnalysisResult());

		SafeCompositionResult targetComposition = checkSafeCompositionOfLine(Constants.TARGET_CK_ALLOY_NAME, targetLine.getFeatures(),
				"target");

		System.out.println("RESULTADO DO WF TARGET: " + !targetComposition.getAnalysisResult());

		return !sourceComposition.getAnalysisResult() && !targetComposition.getAnalysisResult();
		// Fim da checagem de boa formacao
	}

	/**
	 * Verifica se tanto mapeamento quanto conteudo das classes eh igual.
	 * 
	 * @return
	 */
	private boolean isAssetMappingEqual(ProductLine sourceLine, ProductLine targetLine) {
		boolean assetsEqual = false;

		//Checa mepeamento de AM

		//		boolean assetMappingEqual = this.isAssetMappingEqual(sourceLine.getAmPath(), targetLine.getAmPath());

		//Ainda que mapeamento nao seja igual, eh necessario checar quais
		//classes sao diferentes para as otimizacoes.
		//	if(assetMappingEqual){
		//Checa conteudo das classes do AM
		assetsEqual = this.isSameAssets(sourceLine, targetLine);
		//	}

		return assetsEqual;
	}

	private boolean testProducts(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach,
			Criteria criteria, ResultadoLPS resultado) throws IOException, DirectoryException {

		boolean isRefactoring = true;

		try {
			resultado.getMeasures().setQuantidadeProdutosCompilados(0);

			for (Product productSource : sourceLine.getProducts()) {
				if (approach == Approach.NAIVE_2_ICTAC
						|| approach == Approach.NAIVE_1_APROXIMACAO
						|| (approach == Approach.IMPACTED_FEATURES && productSource.containsSomeAsset(this.classesModificaadas, sourceLine
								.getMappingClassesSistemaDeArquivos()))) {
					this.builder.generateProduct(productSource, sourceLine.getPath(), resultado);

					Product provavelCorrespondente = productSource.getLikelyCorrespondingProduct();

					if (provavelCorrespondente != null) {
						this.builder.generateProduct(provavelCorrespondente, targetLine.getPath(), resultado);

						isRefactoring = isRefactoring
								&& CommandLine.isRefactoring(productSource, provavelCorrespondente, sourceLine.getControladoresFachadas(),
										timeout, qtdTestes, approach, criteria, resultado);
					} else {
						isRefactoring = false;
					}

					if (approach != Approach.NAIVE_1_APROXIMACAO && approach != Approach.IMPACTED_FEATURES) {

						//Testa se o comportamento nao bate com nenhum outro destino. Exceto para o caso de NAIVE_WITHOUT_RENAMING.
						if (!isRefactoring) {
							for (Product productTarget : targetLine.getProducts()) {
								if (productTarget != provavelCorrespondente) {
									this.builder.generateProduct(productTarget, targetLine.getPath(), resultado);

									isRefactoring = CommandLine.isRefactoring(productSource, productTarget, sourceLine
											.getControladoresFachadas(), timeout, qtdTestes, approach, criteria, resultado);

									//Para de procurar se encontrar um par com mesmo comportamento.
									if (isRefactoring) {
										break;
									}
								}
							}
						}
					}

					System.out.println(isRefactoring);

					if (!isRefactoring) {
						break;
					}
				}
			}
		} catch (AssetNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("RESULTADO FINAL DO COMPORTAMENTO " + isRefactoring);

		return isRefactoring;
	}

	private HashSet<String> getChangedFeatureNames(ProductLine targetLine) {

		HashSet<String> output = new HashSet<String>();

		if (this.changedAssets == null) {
			return null;
		}

		HashSet<String> changedAssetNames = new HashSet<String>();

		for (String asset : this.changedAssets) {
			String correspondingAssetName = getCorrespondingAssetName(targetLine.getAssetMapping(), asset);
			if (correspondingAssetName != null) {
				changedAssetNames.add(correspondingAssetName);
			}
		}

		ConfigurationKnowledge configutarionKnowledge = targetLine.getCk();

		Set<ConfigurationItem> ckItems = configutarionKnowledge.getCkItems();

		for (ConfigurationItem configurationItem : ckItems) {
			IFeatureExpression featExp = configurationItem.getFeatExp();
			Set<Task> tasks = configurationItem.getTasks();

			for (Task task : tasks) {
				Set<String> provided = task.getProvided().keySet();
				for (String string : changedAssetNames) {
					if (provided.contains(string)) {
						output.add(featExp.getCode());
					}
				}
			}
		}

		return output;
	}

	private String getCorrespondingAssetName(HashMap<String, String> assetMapping, String asset) {
		String result = null;

		asset = asset.replaceAll(Pattern.quote("\\"), "/");

		for (String assetName : assetMapping.keySet()) {
			String assetPath = assetMapping.get(assetName);

			if (assetPath.contains(asset.replaceAll(Pattern.quote("\\"), "/"))) {
				result = assetName;
			}
		}

		return result;
	}

	private boolean isFeatureModelRefinement(String sourceFMXML, String targetFMXML) {

		buildFMEvolutionAlloyFile(sourceFMXML, targetFMXML);
		FeatureModelEvolutionResult checkFMEvolutionAlloyFile = checkFMEvolutionAlloyFile(Constants.ALLOY_PATH
				+ Constants.EVOLUTION_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION);

		return checkFMEvolutionAlloyFile.getAnalysisResult();
	}

	private boolean isAssetMappingRefinement(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach,
			HashSet<String> changedFeatures, Criteria criteria, ResultadoLPS resultado) throws IOException {
		boolean ehAssetMappingRefinement = false;

		try {
			ehAssetMappingRefinement = this.checkAssetMappingBehavior(sourceLine, targetLine, timeout, qtdTestes, approach,
					changedFeatures, criteria, resultado);
		} catch (AssetNotFoundException e1) {
			e1.printStackTrace();
		} catch (DirectoryException e1) {
			e1.printStackTrace();
		}

		return ehAssetMappingRefinement;
	}

	private FeatureModelEvolutionResult checkFMEvolutionAlloyFile(String string) {
		long startedTime = System.currentTimeMillis();
		FeatureModelRefactVerifier instance = FeatureModelRefactVerifier.getInstance();
		boolean fmRefactoring = false;
		try {
			fmRefactoring = instance.isFMRefactoring(string);
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long finishedTime = System.currentTimeMillis();
		FeatureModelEvolutionResult fmResult = new FeatureModelEvolutionResult(startedTime, finishedTime, fmRefactoring);
		return fmResult;
	}

	private SafeCompositionResult checkSafeCompositionOfLine(String string, HashSet<String> features, String name) {
		System.out.println("Inicio do teste de Safe Composition");
		SafeCompositionResult checkCKSource = null;
		try {
			checkCKSource = SafeCompositionVerifier.checkCK(Constants.ALLOY_PATH, string, Constants.ALLOY_EXTENSION, string
					+ Constants.ALLOY_EXTENSION, features, name);
		} catch (Err e) {
			e.printStackTrace();
		}
		System.out.println("Fim do teste de Safe Composition");
		return checkCKSource;
	}

	private void buildFMEvolutionAlloyFile(String sourceFMXML, String targetFMXML) {
		AlloyFMEvolutionBuilder evolutionAlloy = new AlloyFMEvolutionBuilder();
		evolutionAlloy.buildAlloyFile("evolution", Constants.ALLOY_PATH + Constants.EVOLUTION_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION,
				"source", sourceFMXML, "target", targetFMXML);
	}

	private void buildFMAlloyFile(String moduleName, String sourceFmAlloyName, ProductLine productLine) {
		FeatureModelReader featureModelReader = new FeatureModelReader();

		featureModelReader.readFM(moduleName, productLine.getFmPath());

		productLine.setFeatures(featureModelReader.getFeatures());

		if (moduleName.equals("source")) {
			this.sourceFMSemantics = featureModelReader.getSemanticsFM();
		} else if (moduleName.equals("target")) {
			this.targetFMSemantics = featureModelReader.getSemanticsFM();
		}
		try {
			featureModelReader.buildAlloyFile(moduleName, sourceFmAlloyName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void buildAlloyCKFile(String name, String fMSemantics, String indicator, ProductLine productLine) {
		ConfigurationKnowledge ck = productLine.getCk();
		String alloy = ck.toAlloy();

		HashSet<String> correctSet = new HashSet<String>();
		for (String string : productLine.getFeatures()) {
			// correctSet.add(string.trim().toLowerCase());
			correctSet.add(string.trim());
		}
		HashSet<String> ckSigs = ck.getSignatures();
		for (String string : ckSigs) {
			// correctSet.add(string.trim().toLowerCase());
			correctSet.add(string.trim());
		}
		String header = "module " + name + Constants.LINE_SEPARATOR;

		String sigs = "one sig ";
		String separador = "";
		for (String string : correctSet) {
			string = string.trim();
			if (string != null && !string.equals("")) {
				// sigs += separador + string.toLowerCase();
				sigs += separador + string;
				separador = ", ";
			}
		}
		sigs += " in Bool{}" + Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR;

		String assertText = "assert WT {semantica" + indicator + "[] => semanticaCK[]}";
		assertText += Constants.LINE_SEPARATOR;
		assertText += "check WT for 2";

		filesManager.createFile(Constants.ALLOY_PATH + name + Constants.ALLOY_EXTENSION, header + alloy + sigs + fMSemantics
				+ Constants.LINE_SEPARATOR + assertText);
	}

	//	private boolean isFMEqual(String sourceFM, String targetFM) {
	//		FeatureModelReader featureModelReader = new FeatureModelReader();
	//		FeatureModelReader featureModelReaderTarget = new FeatureModelReader();
	//
	//		featureModelReader.readFM("source", sourceFM);
	//		sourceFeatures = featureModelReader.getFeatures();
	//		ArrayList<String> sourceFormulas = featureModelReader.getFormulas();
	//		sourceFMSemantics = featureModelReader.getSemanticsFM();
	//
	//		featureModelReaderTarget.readFM("target", targetFM);
	//		targetFeatures = featureModelReaderTarget.getFeatures();
	//		ArrayList<String> targetFormulas = featureModelReaderTarget
	//		.getFormulas();
	//		targetFMSemantics = featureModelReaderTarget.getSemanticsFM();
	//
	//		buildFMAlloyFile("source", sourceFM, Constants.ALLOY_PATH
	//				+ Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION);
	//		buildFMAlloyFile("target", targetFM, Constants.ALLOY_PATH
	//				+ Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION);
	//
	//		boolean compareFeatures = Comparador.equalSets(sourceFeatures,
	//				targetFeatures);
	//		if (!compareFeatures)
	//			return false;
	//		return Comparador.equalSets(sourceFormulas, targetFormulas);
	//	}

	//	private boolean isCKEqual(String sourceCKXML, String targetCKXML, String sourceAM, String targetAM) {
	//		ConfigurationKnowledge sourceCK = XMLReader.getInstance().getCK(sourceCKXML, sourceAM, this.dependenciasSource);
	//		ConfigurationKnowledge targetCK = XMLReader.getInstance().getCK(targetCKXML, targetAM, this.dependenciasTarget);
	//
	//		return sourceCK.equals(targetCK);
	//	}

	/**
	 * Compara os AM - Supoe um assetname comecando com # no assetmapping e
	 * linha em branco separando
	 * 
	 * @param sourceAssetMapping
	 * @param targetAssetMapping
	 * @return
	 */
	private boolean isAssetMappingEqual(String sourceAssetMapping, String targetAssetMapping) {

		FilesManager manager = FilesManager.getInstance();

		ArrayList<String> sourceMappingContent = manager.getFileContent(sourceAssetMapping);

		ArrayList<String> targetMappingContent = manager.getFileContent(targetAssetMapping);

		TreeMap<String, ArrayList<String>> mappingSource = getMapping(sourceMappingContent);
		TreeMap<String, ArrayList<String>> mappingTarget = getMapping(targetMappingContent);

		if (Comparador.equalSets(mappingSource.keySet(), mappingTarget.keySet())) {
			Set<String> keys = mappingSource.keySet();
			for (String key : keys) {
				ArrayList<String> sourceAssets = mappingSource.get(key);
				ArrayList<String> targetAssets = mappingTarget.get(key);
				if (!Comparador.equalSets(sourceAssets, targetAssets)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private TreeMap<String, ArrayList<String>> getMapping(ArrayList<String> mappingContent) {

		TreeMap<String, ArrayList<String>> mapping = new TreeMap<String, ArrayList<String>>();
		int index = 0;

		while (true) {
			if (index >= mappingContent.size()) {
				break;
			}
			String line = mappingContent.get(index);
			ArrayList<String> mapped = new ArrayList<String>();
			index++;
			while (true) {
				if (index >= mappingContent.size()) {
					break;
				}
				String nextEntry = mappingContent.get(index);
				if (nextEntry.startsWith("#") || nextEntry.equals("")) {
					break;
				}
				mapped.add(nextEntry);
				index++;
			}
			mapping.put(line, mapped);
		}
		return mapping;
	}

	//	/**
	//	 * 
	//	 * @param p
	//	 * @return false se existe diferenca, true se nao
	//	 */
	//	private boolean checkDiff(Product p) {
	//		walkSourceTarget(p.getSourceDir(), p.getTargetDir());
	//
	//		Set<String> sourceKeySet = this.sourceMapping.keySet();
	//		Set<String> targetKeySet = this.targetMapping.keySet();
	//
	//		if (sourceKeySet.equals(targetKeySet)) {
	//			return !doDiff(sourceKeySet, targetKeySet);
	//		}
	//		return false;
	//	}

	public boolean isSameAssets(ProductLine sourceLine, ProductLine targetLine) {
		boolean result = true;

		Set<String> sourceKeySet = sourceLine.getMappingClassesSistemaDeArquivos().keySet();
		Set<String> targetKeySet = targetLine.getMappingClassesSistemaDeArquivos().keySet();

		this.classesModificaadas = new HashSet<String>();
		this.changedAssets = new HashSet<String>();

		for (String asset : sourceKeySet) {
			String locationSource = sourceLine.getMappingClassesSistemaDeArquivos().get(asset);
			String locationTarget = targetLine.getMappingClassesSistemaDeArquivos().get(asset);

			if (locationSource != null && locationTarget != null) {
				File sourceFile = new File(locationSource);
				File targetFile = new File(locationTarget);

				try {
					boolean equals;

					if (asset.endsWith("java")) {
						this.astComparator.setInputs(sourceFile, targetFile);
						equals = this.astComparator.isIsomorphic();
					} else if (asset.endsWith("aj")) {
						equals = this.isTextualFileContentsEquals(sourceFile, targetFile);
					} else {
						//Se nao java ou java, o resultado nao faz diferenca.
						equals = true;
					}

					if (!equals) {
						result = false;
						this.classesModificaadas.add(asset);
						this.changedAssets.add(this.filesManager.getPath("src." + asset));
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (!targetKeySet.containsAll((sourceKeySet))) {
			result = false;
		}

		//		for (String asset : targetKeySet) {
		//			if(!sourceKeySet.contains(asset)){
		//				this.classesModificaadas.add(asset);
		//				this.changedAssets.add(this.filesManager.getPath("src."
		//						+ asset));
		//			}
		//		}

		return result;
	}

	private boolean isTextualFileContentsEquals(File sourceFile, File targetFile) {
		boolean result = true;

		String linhaSource = "";
		String linhaTarget = "";

		try {
			FileReader readerSource = new FileReader(sourceFile);
			FileReader readerTarget = new FileReader(targetFile);

			BufferedReader inSource = new BufferedReader(readerSource);
			BufferedReader inTarget = new BufferedReader(readerTarget);

			while (result && ((linhaSource = inSource.readLine()) != null & (linhaTarget = inTarget.readLine()) != null)) {
				if (!linhaSource.trim().equals(linhaTarget.trim())) {
					result = false;
				}
			}

			if (linhaSource != null || linhaTarget != null) {
				result = false;
			}

			inSource.close();
			inTarget.close();

			readerSource.close();
			readerTarget.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	//	public void walkSourceTarget(String source, String target) {
	//		if (this.sourceMapping == null || this.targetMapping == null) {
	//			this.sourceMapping = new HashMap<String, String>();
	//			this.targetMapping = new HashMap<String, String>();
	//
	//			walkSource(source + Constants.FILE_SEPARATOR + "src");
	//			walkTarget(target + Constants.FILE_SEPARATOR + "src");
	//		}
	//	}

	//	/**
	//	 * true se mudou algo. listas tem que ser iguais
	//	 * 
	//	 * @param source
	//	 * @param target
	//	 * @return
	//	 */
	//	private boolean doDiff(Set<String> source, Set<String> target) {
	//		long value = 0;
	//
	//		for (String asset : source) {
	//			String locationSource = sourceMapping.get(asset);
	//			String locationTarget = targetMapping.get(asset);
	//
	//			try {
	//				value = value + compare(locationSource, locationTarget);
	//
	//				System.out.println("MEU VALUE: " + value);
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		return (value != 0);
	//	}

	/**
	 * Compara dois arquivos.
	 * 
	 * @param locationSource
	 * @param locationTarget
	 * @return Zero se igual.
	 * @throws IOException
	 */
	private long compare(String locationSource, String locationTarget) throws IOException {
		long value;
		String command = "python script.py " + locationSource + " " + locationTarget;
		System.out.println("COMANDO " + command);

		Process process = Runtime.getRuntime().exec(command);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String linha = stdInput.readLine();
		System.out.println(linha);
		value = Long.parseLong(linha);

		return value;
	}

	//	private SafeRefactorResult checkProductBehavior(Product p, int timeout, int qtdTestes, FeaturesKind abordagem, ArrayList<Product> allProducts) {
	//		SafeRefactorResult result = CommandLine.isRefactoring(p.getSourceDir(), p.getTargetDir(), null, timeout, qtdTestes, abordagem);
	//
	//		if(!result.isRefactoring() && abordagem == FeaturesKind.NAIVE){
	//			allProducts.remove(p);
	//
	//			for(int i = 0; i < allProducts.size() && !result.isRefactoring(); i++){
	//				result = CommandLine.isRefactoringCompiledSource(p.getSourceDir(), allProducts.get(i).getTargetDir(), null, timeout, qtdTestes, abordagem);
	//			}
	//		}
	//
	//		return result;
	//	}

	/**
	 * Checa apenas classes modificadas. Se mais de MAX_CLASSES_MODIFICADAS
	 * forem alteradas, a otimizacao eh descartada.
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param changedFeatures
	 * @param criteria
	 * @param totalAnalysisTime
	 * @return
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	private boolean checkAssetMappingBehavior(ProductLine sourceLine, ProductLine targetLine, int timeout, int maxTests, Approach approach,
			HashSet<String> changedFeatures, Criteria criteria, ResultadoLPS resultado) throws IOException, AssetNotFoundException,
			DirectoryException {
		boolean sameBehavior = false;

		long startedTime = System.currentTimeMillis();

		if (this.classesModificaadas.size() > MAX_CLASSES_MODIFICADAS) {
			System.out.println("################ A quantidade de classes modificadas eh maior que " + MAX_CLASSES_MODIFICADAS
					+ ". A otimizacao para AM nao serah realizada.");
		} else {
			resultado.getMeasures().setQuantidadeProdutosCompilados(1);

			String productPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR;

			// Criando pastas para source e target, que ser�o as mesmas para 
			//todas as classes modificadas e suas dependencias.
			File testSourceDirectory = FilesManager.getInstance().createDir(
					productPath + "source" + Constants.FILE_SEPARATOR + (line == Lines.TARGET ? "src" : ProductBuilder.SRCPREPROCESS));

			File testTargetDirectory = FilesManager.getInstance().createDir(
					productPath + "target" + Constants.FILE_SEPARATOR + (line == Lines.TARGET ? "src" : ProductBuilder.SRCPREPROCESS));

			// Criando pasta bin.
			FilesManager.getInstance().createDir(productPath + "source" + Constants.FILE_SEPARATOR + "bin");
			FilesManager.getInstance().createDir(productPath + "target" + Constants.FILE_SEPARATOR + "bin");

			String libPathSource = sourceLine.getLibPath();
			String libPathtarget = targetLine.getLibPath();

			if (libPathSource != null && libPathtarget != null) {
				FilesManager.getInstance().copyLibs(libPathSource, productPath + "source" + Constants.FILE_SEPARATOR + "lib");
				FilesManager.getInstance().copyLibs(libPathtarget, productPath + "target" + Constants.FILE_SEPARATOR + "lib");
			}

			//Classes que terao testes gerados.
			String classes = "";
			String classeToGenerateTestes = "";

			this.listaAspectos = new HashSet<String>();

			for (String classe : this.classesModificaadas) {
				System.out.println("################ " + this.classesModificaadas.size() + "Verificando classe " + classe);

				String fileSourcePath = sourceLine.getMappingClassesSistemaDeArquivos().get(classe);
				String fileTargetPath = targetLine.getMappingClassesSistemaDeArquivos().get(classe);

				// Copiando versao source do arquivo modificado.
				String destinationPath = null;
				File fileDestination = null;

				destinationPath = testSourceDirectory.getAbsolutePath()
						+ FilesManager.getInstance().getPathAPartirDoSrc(fileSourcePath).replaceFirst("src", "");

				fileDestination = new File(destinationPath);

				classeToGenerateTestes = classe;

				if (this.line == Lines.TARGET) {
					classeToGenerateTestes = FilesManager.getInstance().getPathAPartirDoSrc(classeToGenerateTestes).replaceFirst(
							Pattern.quote("src.java."), "");
				}

				FilesManager.getInstance().createDir(fileDestination.getParent());
				FilesManager.getInstance().copyFile(fileSourcePath, fileDestination.getAbsolutePath());

				// Pegando path da classe na versao source.
				File fileSource = fileDestination;

				destinationPath = testTargetDirectory.getAbsolutePath()
						+ FilesManager.getInstance().getPathAPartirDoSrc(fileTargetPath).replaceFirst("src", "");

				fileDestination = new File(destinationPath);

				FilesManager.getInstance().createDir(fileDestination.getParent());
				FilesManager.getInstance().copyFile(fileTargetPath, fileDestination.getAbsolutePath());

				// Pegando path da classe na versao source.
				File fileTarget = fileDestination;

				this.dependenciasCopiadas = new HashSet<String>();

				this.copyDependencies(fileSource, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(), null, sourceLine
						.getDependencias());
				this.copyDependencies(fileTarget, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), null, targetLine
						.getDependencias());

				//Como aspectos nao sao pre-processados, caso existam constantes de pre-processamentos
				//nas classes do conjunto, serah necessario mover manualmente os aspectos para a pasta
				//src apos o pre-processamento.
				if (fileSource.getAbsolutePath().endsWith("aj")) {
					this.listaAspectos.add(fileSource.getAbsolutePath());
				}
				if (fileTarget.getAbsolutePath().endsWith("aj")) {
					this.listaAspectos.add(fileTarget.getAbsolutePath());
				}

				if (classes.equals("")) {
					classes = classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
				} else if (!classes.contains(classeToGenerateTestes.replaceAll(".java", ""))) {
					classes = classes + "|" + classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
				}
			}

			ArrayList<Product> produtosQueContemClassesModificadas = new ArrayList<Product>();

			//Cheque somente produtos que tem alguma das classes modificadas.
			for (Product product : sourceLine.getProducts()) {
				if (product.containsSomeAsset(this.classesModificaadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
					produtosQueContemClassesModificadas.add(product);
				}
			}

			//Se tiver aspectos ou tags de pre-processamento no codigo, faz um for com todos os produtos possiveis.
			//Filtrar entre os produtos poss�veis, removendo os que tiverem mesmos conjuntos de aspectos que interferem

			//		**Eh melhor entrar aqui se temAspectos, mesmo que nao tenha preprocess
			//		**Quando tiver soh preprocess entra no else
			//		**
			if (sourceLine.temAspectos()) {
				HashSet<HashSet<String>> products = new HashSet<HashSet<String>>();
				
				produtosQueContemClassesModificadas.clear();

				for (Product product : sourceLine.getProducts()) {
					if (product.containsSomeAsset(this.classesModificaadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
						produtosQueContemClassesModificadas.add(product);

						products.add(product.getFeaturesList());
					}
				}
				//				HashSet<HashSet<String>> products = sourceLine.getSetsOfFeatures();
				//
				//				products = this.builder.filter(products, changedFeatures);

				//Se tem aspectos, mas nao existem classes com mais de uma versao na linha.
				HashSet<HashSet<String>> pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();

				this.mapeamentoFeaturesAspectosSource = new HashMap<HashSet<String>, HashSet<String>>();
				this.mapeamentoFeaturesAspectosTarget = new HashMap<HashSet<String>, HashSet<String>>();

				for (HashSet<String> product : products) {
					HashSet<String> prodToBuild = this.makeCombination(product, sourceLine, targetLine);

					if (prodToBuild.size() > 0) {
						pseudoProductsToBePreprocessed.add(prodToBuild);
					}
				}

				resultado.getMeasures().setQuantidadeProdutosCompilados(pseudoProductsToBePreprocessed.size());

				sameBehavior = true;

				System.out.println("######################PRODUCTS SIZE " + pseudoProductsToBePreprocessed.size());
				int i = 0;
				for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
					System.out.println("######################PRODUCT " + i++);
					HashSet<String> aspectosDaConfiguracaoSource = this.mapeamentoFeaturesAspectosSource.get(prod);
					HashSet<String> aspectosDaConfiguracaoTarget = this.mapeamentoFeaturesAspectosTarget.get(prod);

					ArrayList<File> filesToTrash = new ArrayList<File>();

					for (String aspecto : aspectosDaConfiguracaoSource) {
						String destinationPath = testSourceDirectory.getAbsolutePath() + aspecto.split("src")[1];

						if (!this.classesModificaadas.contains(aspecto) && !this.listaAspectos.contains(destinationPath)) {
							File fileDestination = new File(destinationPath);

							FilesManager.getInstance().createDir(fileDestination.getParent());
							FilesManager.getInstance()
									.copyFile(
											sourceLine.getMappingClassesSistemaDeArquivos().get(
													FilesManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());

							this.copyDependencies(fileDestination, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(),
									filesToTrash, sourceLine.getDependencias());
						}
					}

					for (String aspecto : aspectosDaConfiguracaoTarget) {
						String destinationPath = testTargetDirectory.getAbsolutePath() + aspecto.split("src")[1];

						if (!this.classesModificaadas.contains(aspecto) && !this.listaAspectos.contains(aspecto)) {
							File fileDestination = new File(destinationPath);

							FilesManager.getInstance().createDir(fileDestination.getParent());
							FilesManager.getInstance()
									.copyFile(
											targetLine.getMappingClassesSistemaDeArquivos().get(
													FilesManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());

							this.copyDependencies(fileDestination, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(),
									filesToTrash, targetLine.getDependencias());
						}
					}

					if (this.line == Lines.TARGET) {
						//Para a TaRGeT, os arquivos sao pre processados com o Velocity dentro da propria
						//pasta, sem copias. Com isso, eh necessario renomear a pasta para src manualmente.

						if (prod.size() > 0/* && !(prod.size() == 1 && prod.iterator().next().contains("fake"))*/) {
							this.builder.preprocessVelocity(prod, testSourceDirectory, sourceLine, testSourceDirectory.getParent(),
									resultado);
							this.builder.preprocessVelocity(prod, testTargetDirectory, targetLine, testTargetDirectory.getParent(),
									resultado);
						}
					} else {
						//Para o MobileMedia, pre processa com Antenna, que copia o codigo da pasta
						//srcprecess para src.
						this.builder.preprocess(this.builder.getSymbols(prod), testSourceDirectory.getParent(), resultado);

						this.builder.preprocess(this.builder.getSymbols(prod), testTargetDirectory.getParent(), resultado);

						System.out.println("#######################################################" + prod);

						//Como o Antenna nao pre processa aspectos, eles tambem nao sao copiados para 
						//a pasta src. Eh necessario copia-los manualmente.
						for (String aspect : this.listaAspectos) {
							String dir = new File(aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src")).getParentFile()
									.getAbsolutePath();
							FilesManager.getInstance().createDir(dir);
							FilesManager.getInstance().copyFile(aspect, aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src"));
						}
					}

					sameBehavior = sameBehavior
							&& CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(), classes,
									timeout, maxTests, approach, criteria, sourceLine.getPath(), targetLine.getPath(), resultado, false,
									false);

					for (File file : filesToTrash) {
						file.delete();
						this.listaAspectos.remove(file.getAbsolutePath());

						this.dependenciasCopiadas.remove(file.getAbsolutePath());
					}

					filesToTrash.clear();

					System.out.println("###########################" + sameBehavior);
					
					if(!sameBehavior){
						break;
					}
				}
			} else {
				//Entra aqui sempre que nao tem Aspectos.
				//Pode haver pre processamento.
				//Soh MobileMediaOO entra aqui

				this.constantsPreProcessor = new HashSet<String>();
				this.getPreProcessorConstantes(testSourceDirectory);

				if (this.constantsPreProcessor.size() > 0) {
					HashSet<HashSet<String>> products = sourceLine.getSetsOfFeatures();

					products = this.builder.filter(products, changedFeatures);

					HashSet<HashSet<String>> pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();

					for (HashSet<String> product : products) {
						HashSet<String> prodToBuild = this.getFeaturesEmComum(product, this.constantsPreProcessor);

						if (prodToBuild.size() > 0) {
							pseudoProductsToBePreprocessed.add(prodToBuild);
						}
					}

					resultado.getMeasures().setQuantidadeProdutosCompilados(pseudoProductsToBePreprocessed.size());

					sameBehavior = true;

					for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
						this.builder.preprocess(this.builder.getSymbols(prod), testSourceDirectory.getParent(), resultado);

						this.builder.preprocess(this.builder.getSymbols(prod), testTargetDirectory.getParent(), resultado);

						sameBehavior = sameBehavior
								&& CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(),
										classes, timeout, maxTests, approach, criteria, sourceLine.getPath(), targetLine.getPath(),
										resultado, false, false);

						System.out.println("###########################" + sameBehavior);
					}
				} else {
					//Se nem tem aspectos nem preprocessamento.
					testSourceDirectory.renameTo(new File(testSourceDirectory.getParent() + Constants.FILE_SEPARATOR + "src"));
					testTargetDirectory.renameTo(new File(testTargetDirectory.getParent() + Constants.FILE_SEPARATOR + "src"));

					sameBehavior = CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(),
							classes, timeout, maxTests, approach, criteria, sourceLine.getPath(), targetLine.getPath(), resultado, false,
							false);

					System.out.println("###########################" + sameBehavior);
				}
			}

			long finishedTime = System.currentTimeMillis();

			System.out.println("Asset mapping verificado em: " + String.valueOf((finishedTime - startedTime) / 1000) + " segundos.");
		}

		return sameBehavior;
	}

	/**
	 * Conjuntos de features mapeados para conjuntos de Aspectos.
	 * 
	 * @param products
	 * @param am
	 * @param ck
	 * @return
	 * @throws AssetNotFoundException
	 * @throws IOException
	 */
	private HashMap<HashSet<String>, HashSet<String>> getConjuntosDeAspectosPossiveis(HashSet<HashSet<String>> products,
			ProductLine productLine) throws IOException, AssetNotFoundException {

		HashMap<HashSet<String>, HashSet<String>> result = new HashMap<HashSet<String>, HashSet<String>>();

		for (HashSet<String> product : products) {
			Set<String> constantesArquivos = productLine.getCk().evalCKDestinos(product).keySet();

			ArrayList<String> aspectosDaConfiguracao = new ArrayList<String>();

			for (String constante : constantesArquivos) {
				String file = productLine.getAssetMapping().get(constante.trim());

				if (file.endsWith(".aj")) {
					aspectosDaConfiguracao.add(file);
				}
			}

			//Checa quais aspectos influenciam no conjunto de classes modificadas. 
			//Soh eles importam.

			HashSet<String> aspectosQueInterferem = this.getAspectosQueInterferemNaClasseModificada(aspectosDaConfiguracao, productLine
					.getAssetMapping());

			result.put(product, aspectosQueInterferem);
		}

		return result;
	}

	private HashSet<String> getFeaturesEmComum(HashSet<String> product, HashSet<String> constantsPreProcessor) {
		HashSet<String> result = new HashSet<String>();

		for (String constant : constantsPreProcessor) {
			String feat = this.builder.getPreprocessConstantsToFeatures().get(constant);

			if (product.contains(feat)) {
				result.add(feat);
			}
		}

		return result;
	}

	private String getPathName(String classe) {
		classe = classe.replaceAll(Pattern.quote("."), "/");

		return "/src/" + classe.replaceAll(Pattern.quote("/java"), ".java");
	}

	private HashSet<String> makeCombination(HashSet<String> product, ProductLine sourceLine, ProductLine targetLine) {
		HashSet<String> result = new HashSet<String>();

		//Como o mapeamento nao mudou, os aspectos da versao source e da target sao os mesmos.
		ArrayList<String> aspectosDoProdutoSource = this.getAspectosDoProduto(product, sourceLine);
		//	ArrayList<String> aspectosDoProdutoTarget = this.getAspectosDoProduto(product, targetCKXML, targetAM);

		//Os que interferem podem ter mudado j� que o conte�do deles mudou, podento ter pointcuts
		//e advices novos.
		HashSet<String> aspectosQueInterferemNaClasseModificadaSource = this.getAspectosQueInterferemNaClasseModificada(
				aspectosDoProdutoSource, sourceLine.getMappingClassesSistemaDeArquivos());
		HashSet<String> aspectosQueInterferemNaClasseModificadaTarget = this.getAspectosQueInterferemNaClasseModificada(
				aspectosDoProdutoSource, targetLine.getMappingClassesSistemaDeArquivos());

		for (String feature : product) {
			String feat = this.builder.getPreprocessFeaturesToConstants().get(feature);

			if (feat != null) {
				result.add(feature);
			}
		}

		boolean jaExiste = false;

		//Verifica se o mesmo conjunto de features j� foi mapeado em outro conjunto de aspectos.
		if (!this.mapeamentoFeaturesAspectosSource.values().contains(aspectosQueInterferemNaClasseModificadaSource)) {
			if (this.mapeamentoFeaturesAspectosSource.containsKey(result) || result.isEmpty()) {
				result.add("fake" + Math.random());
			}

			this.mapeamentoFeaturesAspectosSource.put(result, aspectosQueInterferemNaClasseModificadaSource);
			this.mapeamentoFeaturesAspectosTarget.put(result, aspectosQueInterferemNaClasseModificadaTarget);
		} else {
			for (HashSet<String> prodKey : this.mapeamentoFeaturesAspectosSource.keySet()) {
				if (this.mapeamentoFeaturesAspectosSource.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaSource)
						&& this.mapeamentoFeaturesAspectosTarget.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaTarget)) {

					jaExiste = this.equivalent(prodKey, result);
				}
			}

			if (!jaExiste) {
				if (this.mapeamentoFeaturesAspectosSource.containsKey(result) || result.isEmpty()) {
					result.add("fake" + Math.random());
				}

				this.mapeamentoFeaturesAspectosSource.put(result, aspectosQueInterferemNaClasseModificadaSource);
				this.mapeamentoFeaturesAspectosTarget.put(result, aspectosQueInterferemNaClasseModificadaTarget);
			}
		}

		return result;
	}

	private boolean equivalent(HashSet<String> prodKeyAntiga, HashSet<String> resultNovo) {
		//A prodKeyAntida pode ter feature fake.
		boolean result = true;

		for (String feat : prodKeyAntiga) {
			if (!resultNovo.contains(feat) && !feat.contains("fake")) {
				result = false;
			}

			if (!result) {
				break;
			}
		}

		if (result) {
			for (String feat : resultNovo) {
				if (!prodKeyAntiga.contains(feat)) {
					result = false;
				}

				if (!result) {
					break;
				}
			}
		}

		return result;
	}

	private HashSet<String> getAspectosQueInterferemNaClasseModificada(ArrayList<String> aspectosDoProduto, HashMap<String, String> mapping) {

		HashSet<String> aspectosQueInterferemNaClasseModificada = new HashSet<String>();

		for (String aspecto : aspectosDoProduto) {
			//As classes em que um aspecto interferem sao informadas na primeira
			//linha do arquivo aj.

			String path = mapping.get(FilesManager.getInstance().getCorrectName(this.replaceBarrasPorSeparator(aspecto)));

			Collection<String> classesEmQueOAspectoInterfe = FilesManager.getInstance().getDependenciasAspectos(new File(path));

			for (String classeModificada : this.classesModificaadas) {

				for (String classeEmQueAspectoInterfere : classesEmQueOAspectoInterfe) {
					if (classeModificada.contains(classeEmQueAspectoInterfere)) {
						aspectosQueInterferemNaClasseModificada.add(aspecto);
					}
				}
			}
		}

		return aspectosQueInterferemNaClasseModificada;
	}

	private String replaceBarrasPorSeparator(String aspecto) {
		String result = "";

		String[] parts = aspecto.split(Pattern.quote("/"));

		for (String part : parts) {
			if (result.equals("")) {
				result = part;
			} else {
				result = result + Constants.FILE_SEPARATOR + part;
			}

		}

		return result;
	}

	private ArrayList<String> getAspectosDoProduto(HashSet<String> product, ProductLine sourceLine) {
		ArrayList<String> aspectosDoProduto = new ArrayList<String>();

		ConfigurationKnowledge sourceCK = sourceLine.getCk();
		HashMap<String, String> evalCKSource = sourceCK.evalCKDestinos(product);

		for (String constant : evalCKSource.keySet()) {
			//Considerando que o mapeamento seja se 1:1.

			String asset = sourceLine.getAssetMapping().get(constant.trim());

			if (asset.endsWith("aj")) {
				aspectosDoProduto.add(asset);
			}
		}

		return aspectosDoProduto;
	}

	private void getPreProcessorConstantes(File file) throws IOException {
		if (file.isDirectory()) {
			for (File fileFromList : file.listFiles()) {
				this.getPreProcessorConstantes(fileFromList);
			}
		} else {
			String linha = "";

			FileReader reader = new FileReader(file);

			BufferedReader in = new BufferedReader(reader);

			while ((linha = in.readLine()) != null) {
				if (linha.contains("#if") || linha.contains("#elif")) {
					linha = linha.trim();

					String[] parts = linha.split(" ");

					for (String part : parts) {

						//Se nao eh um ifdef, elif ou simbolo logico (||, &&, etc)
						if (!part.contains("#") && part.length() > 2) {
							this.constantsPreProcessor.add(part);
						}
					}
				}
			}

			in.close();
			reader.close();
		}
	}

	private Collection<String> getDependeciasConstrutor(String location) {
		Collection<String> result = new ArrayList<String>();

		if (location.endsWith("java")) {
			File sourceFile = new File(location);

			try {
				this.astComparator.setInput(sourceFile);
				result = this.astComparator.getConstructorParameters();

			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	private void copyDependencies(File classe, File destinationDirectory, HashMap<String, String> mapping, ArrayList<File> filesToTrash,
			HashMap<String, Collection<String>> dependenciasCache) throws AssetNotFoundException, DirectoryException {

		String pathDependencia = FilesManager.getInstance().getPathAPartirDoSrc(
				classe.getAbsolutePath().replaceFirst("srcpreprocess", "src"));

		if (!pathDependencia.startsWith("/")) {
			pathDependencia = "/" + pathDependencia;
		}

		Collection<String> dependencias = dependenciasCache.get(pathDependencia.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/"));

		if (dependencias == null) {
			if (classe.getAbsolutePath().endsWith(".java")) {

				dependencias = Main.v().getDependences(classe.getName().replaceAll(".java", ""), classe.getParent());

				//Classes podem ser dependentes de aspectos
				//Ocorre quando excecoes lancadas em classes sao tratadas apenas em Aspectos.
				dependencias.addAll(FilesManager.getInstance().getDependenciasAspectos(classe));
			} else {
				//Dependencias de aspectos serao identificadas pelo import deles.
				dependencias = FilesManager.getInstance().getDependenciasDeAspectosPeloImport(classe);
			}
		}

		for (String dependecia : dependencias) {
			if (!classe.getAbsolutePath().contains(dependecia)
					|| (!dependecia.contains(".") && !classe.getAbsolutePath().contains("." + dependecia))) {

				String path = this.getPathClassMapping(dependecia, mapping);

				if (path != null) {
					File file = new File(path);

					// destinationFolder inclui as pastas intermediarias ate o arquivo.
					// destinationDirectory soh vai ate source ou target.
					String destinationFolder = destinationDirectory.getAbsolutePath()
							+ FilesManager.getInstance().getPathAPartirDoSrc(file.getAbsolutePath()).replaceFirst("src", "");

					File fileDestination = new File(destinationFolder);

					if (this.dependenciasCopiadas.add(fileDestination.getAbsolutePath())) {
						FilesManager.getInstance().createDir(fileDestination.getParent());

						if (!fileDestination.exists()) {
							FilesManager.getInstance().copyFile(file.getAbsolutePath(), fileDestination.getAbsolutePath());

							if (fileDestination.getAbsolutePath().endsWith("aj")) {
								this.listaAspectos.add(fileDestination.getAbsolutePath());
							}

							if (filesToTrash != null) {
								filesToTrash.add(fileDestination);
								filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS,
										"src")));
							}

							this.copyDependencies(file, destinationDirectory, mapping, filesToTrash, dependenciasCache);
						}
					}
				}
			}
		}
	}

	private String getPathClassMapping(String dependecia, HashMap<String, String> mapping) {
		String result = null;

		// A dependencia nem sempre eh um nome de classe com pacote. As chaves do
		// mapping sempre sao.
		// Eh necessario checar se dependencia faz parte de alguma key.
		for (String key : mapping.keySet()) {
			if ((dependecia.contains(".") && key.endsWith(dependecia + ".java")) || key.endsWith("." + dependecia + ".java")
					|| (dependecia.contains(".") && key.endsWith(dependecia + ".aj")) || key.endsWith("." + dependecia + ".aj")) {
				result = mapping.get(key);

				break;
			}
		}

		return result;
	}

	public long getTestsCompileTimeout() {
		return testsCompileTimeout;
	}

	public long getTestsExecutionTimeout() {
		return testsExecutionTimeout;
	}

	public long getTestsGenerationTimeout() {
		return testsGenerationTimeout;
	}

	/**
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param qtdTestes
	 * @param selectedApproaches
	 * @param temAspectosSource
	 * @param temAspectosTarget
	 * @param controladoresFachadas
	 * @param criteria
	 * @param sourceCKKind
	 * @param targetCKKind
	 * @param sourceAMFormat
	 * @param targetAMFormat
	 * @param resultado
	 * @return
	 * @throws Err
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	public boolean verifyLine(String sourcePath, String targetPath, int timeout, int qtdTestes, Approach selectedApproaches, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, ResultadoLPS resultado) throws Err, IOException, AssetNotFoundException, DirectoryException {
		//	sourcePath = sourcePath.startsWith("/") ? sourcePath : Constants.PLUGIN_PATH + "/../Exemplos/" + sourcePath;
	//	targetPath = targetPath.startsWith("/") ? targetPath : Constants.PLUGIN_PATH + "/../Exemplos/" + targetPath;

		ProductLine souceLine = new ProductLine(sourcePath, sourcePath + "/ck.xml", sourcePath + "/fm.xml", sourcePath + "/am.txt",
				temAspectosSource, controladoresFachadas, sourceCKKind, sourceAMFormat);

		ProductLine targetLine = new ProductLine(targetPath, targetPath + "/ck.xml", targetPath + "/fm.xml", targetPath + "/am.txt",
				temAspectosTarget, controladoresFachadas, targetCKKind, targetAMFormat);

		return this.verifyLine(souceLine, targetLine, timeout, qtdTestes, selectedApproaches, criteria, resultado);
	}

	/**
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param qtdTestes
	 * @param selectedApproaches
	 * @param temAspectosSource
	 * @param temAspectosTarget
	 * @param controladoresFachadas
	 * @param criteria
	 * @param sourceCKKind
	 * @param targetCKKind
	 * @param sourceAMFormat
	 * @param targetAMFormat
	 * @param resultado
	 * @param libPathSource
	 * @param libPathTarget
	 * @return
	 * @throws Err
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	public boolean verifyLine(String sourcePath, String targetPath, int timeout, int qtdTestes, Approach selectedApproaches, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, ResultadoLPS resultado, String libPathSource, String libPathTarget) throws Err, IOException, AssetNotFoundException, DirectoryException {

		ProductLine sourceLine = new ProductLine(sourcePath, sourcePath + "/ck.xml", sourcePath + "/fm.xml", sourcePath + "/am.txt", temAspectosSource, controladoresFachadas, sourceCKKind, sourceAMFormat);

		sourceLine.setLibPath(libPathSource);

		ProductLine targetLine = new ProductLine(targetPath, targetPath + "/ck.xml", targetPath + "/fm.xml", targetPath + "/am.txt", temAspectosTarget, controladoresFachadas, targetCKKind, targetAMFormat);

		targetLine.setLibPath(libPathTarget);

		return this.verifyLine(sourceLine, targetLine, timeout, qtdTestes, selectedApproaches, criteria, resultado);
	}

}
