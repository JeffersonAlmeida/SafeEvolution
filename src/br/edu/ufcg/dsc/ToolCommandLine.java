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
import java.util.Iterator;
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

	/*What does it means ? */
	private String sourceFMSemantics;
	/*What does it means ? */
	private String targetFMSemantics;
	
	/* This instance managers the files and look for its dependencies needed to compile: Class, Aspects. In addition, it copies the file from the source to the target product. Like hephaestus tool does.*/
	private FilesManager filesManager;
	
	/*Hashset of dependencies to the compile the file. */
	private HashSet<String> dependenciasCopiadas;

	/*A string collection of changed classes.*/
	private Collection<String> classesModificaadas;
	
	/**/
	private ProductBuilder builder;
	
	private long testsCompileTimeout;
	
	private long testsExecutionTimeout;
	private long testsGenerationTimeout;
	
	/* This variable will store the changed assets - mofified files/classes.*/
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

	public boolean verifyLine(ProductLine souceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach, Criteria criteria, ResultadoLPS resultado) throws Err, IOException, AssetNotFoundException, DirectoryException {

		/* Till here this is not a refinement yet. */
		boolean isRefinement = false;

	 	/* cleans the generated products folder. */
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

	/**
	 * @param sourceLine Source Product Line.
	 * @param targetLine Target Product Line.
	 */
	private void putProductsInCache(ProductLine sourceLine, ProductLine targetLine) {
		System.out.println("\n\n\n\t\tLet's put the products in cache.\n");
		if (this.cacheProducts.get(sourceLine.getPath()) == null || this.cacheProducts.get(targetLine.getPath()) == null) {
			/*Build the Source Feature Model Alloy file.*/
			System.out.println("\nBuild the SOURCE Feature Model Alloy file:");
			buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
			
			/*Build the Target Feature Model Alloy file.*/
			System.out.println("\nBuild the TARGET Feature Model Alloy file:");
			buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);

			/*Build the Evolution Alloy file.*/
			System.out.println("\nBuild the EVOLUTION Alloy file:");
			buildFMEvolutionAlloyFile(sourceLine.getFmPath(), targetLine.getFmPath());

			if (this.cacheProducts.get(sourceLine.getPath()) == null) {
				HashSet<HashSet<String>> productsSource = this.builder.getProductsFromAlloy(Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME);
				this.cacheProducts.put(sourceLine.getPath(), productsSource);
			}

			if (this.cacheProducts.get(targetLine.getPath()) == null) {
				HashSet<HashSet<String>> productsTarget = this.builder.getProductsFromAlloy(Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME);
				this.cacheProducts.put(targetLine.getPath(), productsTarget);
			}

		}

		sourceLine.setSetsOfFeatures(this.cacheProducts.get(sourceLine.getPath()));
		targetLine.setSetsOfFeatures(this.cacheProducts.get(targetLine.getPath()));
		System.out.println("\n\t\tThe products are already in cache.");
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

		/* Removes all of the mappings from this map. The map will be empty after this call returns. */
		XMLReader.getInstance().reset();

		/* cleans the generated products folder. */
		this.cleanProducts();

		souceLine.setup();
		targetLine.setup();
	}

	/**
	 * This method cleans the generated products folder.
	 */
	private void cleanProducts() {
		
		/*It creates an ANT build file in the tool directory + ant/build.xml*/
		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");

		/*It creates a new ANT project.*/
		Project p = new Project();
		
		/*This is the directory of the generated products: */
		System.out.println("\nThe directory of the generated products: " + "<  Tool Path + Products  >\n");
		
		/* Set an ANT Build XML File property. Any existing property of the same name is overwritten, unless it is a user property.*/
		
		/*  productsFolder =>                   The name of property to set. 
		 *  Constants.PRODUCTS_DIR =>           The new value of the property.*/
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
		/*It cleans all of the product directories.*/ /*Two directories have been deleted: productsFolder and ${pluginpath}/emma/instr/.*/
		// productsFolder =>               Tool Path + Products
		//                =>               pluginpath + /emma/instr/
		System.out.println("\n Two directories have been deleted:  < Tool Path + Products > and < pluginpath + emma + instr >");
	}

	/**
	 * One of the main methods of this class. It is responsible to check the SPL. <br></br>
	 * @param sourceLine Source SPL.  <br></br>
	 * @param targetLine Target SPL.  <br></br>
	 * @param timeout  <br></br>
	 * @param qtdTestes Amount of applied tests per methods. <br></br>
	 * @param approach The approach used to check the SPL Evolution. <br></br>
	 * @param criteria  <br></br>
	 * @param resultado SPL Evolution Results/Report  <br></br>
	 * @return Returns whether the SPL evolution is a refinement  <br></br>
	 * @throws IOException Case this is not successful, throws IO Exception.  <br></br>
	 * @throws AssetNotFoundException Case this is not successful, throws IO Asset Not Found Exception. <br></br>
	 * @throws DirectoryException  Case this is not successful, throws Directory Exception. <br></br>
	 */
	public boolean checkLPS(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach, Criteria criteria, ResultadoLPS resultado) throws IOException, AssetNotFoundException, DirectoryException {
		
		/* Till here this is not a refinment yet. */
		boolean isRefinement = false;

		/* Check whether the Software Product Line is well formed. */
		System.out.println("\n\n\n\n\t\tLet's check if the SPL is well formed.\n");
		boolean isWF = this.isWF(sourceLine, targetLine);
		System.out.println("\n\n\n\n\t\tOk. We have already checked the well formedness.\n");
		
		/* Set this property in Results Class - To be used as a report later.*/
		resultado.setWF(isWF);

		/*Verify Feature Model -  FM is not verified for the Naive Approach, ONLY Well Formedness*/
		if (isWF) {
			/* Verify whether Configuration knowledge and Feature Model is a refinement */
			boolean isFMAndCKRefinement = this.isFeatureModelAndConfigurationKnowledgeWeakRefinement(sourceLine, targetLine);
			
			System.out.println("FM and CK are refinement:- " + isFMAndCKRefinement );
			
			/*Set this information in the Results/Short Report.*/
			resultado.setFMAndCKRefinement(isFMAndCKRefinement);

			/*which approach is settled ? */
			if (approach == Approach.NAIVE_2_ICTAC || approach == Approach.NAIVE_1_APROXIMACAO || isFMAndCKRefinement) {

				/* verify whether two AM's is equal. */
				boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceLine, targetLine);
				/* Set it down in the results. */
				resultado.setAssetMappingsEqual(isAssetMappingsEqual);

				if (approach == Approach.NAIVE_2_ICTAC || approach == Approach.NAIVE_1_APROXIMACAO || !isAssetMappingsEqual) {

					HashSet<String> changedFeatures = null;

					if (approach == Approach.IMPACTED_FEATURES || approach == Approach.ONLY_CHANGED_CLASSES) {
						/* Generate tests only for impacted features. */
						changedFeatures = getChangedFeatureNames(targetLine);
					}

					resultado.getMeasures().getTempoExecucaoAbordagem().startContinue();

					if (approach == Approach.ONLY_CHANGED_CLASSES) {
						/* Only generates tests for modified classes and do not generate products. */
						/* Generates twice the test  amount per method. */
						isRefinement = this.isAssetMappingRefinement(sourceLine, targetLine, timeout, qtdTestes, approach, changedFeatures, criteria, resultado);
					} else {
						/* Generate tests for all classes and all products. */
						isRefinement = this.testProducts(sourceLine, targetLine, timeout, qtdTestes, approach, criteria, resultado);
					}
				
					resultado.getMeasures().getTempoExecucaoAbordagem().pause();
					/*Set whether the SPL evolution is a refinement. */
					resultado.getMeasures().setResult(isRefinement);

					if (isRefinement) {
						System.out.println("\nThe Software Product Line is a refinement.\n");
					} else {
						System.out.println("The Software Product Line is NOT a refinement.\nAsset Mapping was not refined.\n");
					}
				} else {
					/*Set whether the SPL evolution is a refinement. */
					resultado.getMeasures().setResult(true);
					System.out.println("\nThe Software Product Line was refined.\n");
				}
			} 
			/* The approaches that can be applied to check the refinement is NAIVE_2_ICTAC and NAIVE_1_APROXIMACAO. */
			/* However, if CK and FM is a refinement, it is possible to check it out with the two others aproaches. */
			else {
				/* This approach can not be applied. */
				System.out.println("The " + approach + " approach can not be applied.\nFM' and CK' don't refine FM and CK.");
			}
		} else {
			System.out.println("\n Software Product Line Short Report :\n");
		    System.out.println("- It is NOT a refinement.\n");
		    System.out.println("- It is NOT well formed.\n");
			/* Software Product Line Short Report : */
			/* It is not a refinement. It is not well formed. */
		}
		return isRefinement;
	}

	/**
	 * Verify whether Configuration knowledge and Feature Model is a refinement.<br></br>
	 * @param sourceLine
	 * @param targetLine
	 * @return
	 * @throws IOException
	 * @throws AssetNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private boolean isFeatureModelAndConfigurationKnowledgeWeakRefinement(ProductLine sourceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
	
		boolean isRefinement = true;

		/*IT will store the set of SOURCE PRODUCT LINE features*/
		HashSet<HashSet<String>> setsOfFeaturesSource = sourceLine.getSetsOfFeatures();
		
		/*IT will store the set of TARGET PRODUCT LINE features*/
		HashSet<HashSet<String>> setsOfFeaturesTarget = targetLine.getSetsOfFeatures();

		/* A clone of the variable above*/
		setsOfFeaturesTarget = (HashSet<HashSet<String>>) setsOfFeaturesTarget.clone();

		/* The product Id.*/
		int id = 0;

		for (HashSet<String> featureSetSource : setsOfFeaturesSource) {
			/* It will evaluate CK and AM in order to produce products of the SPL. */
			Product productSource = this.evaluateProductCKAM(featureSetSource, sourceLine, id++);
			
			/* It will add the new generated product in the set of products of the <SOURCE> product line. */
			sourceLine.getProducts().add(productSource);

			/* This part is trying to accomplish the step 1 and 2 of all product pairs approach.
			 * Step 2 - Mapping corresponding products.
               Step 3 - Generations Target products with their corresponding source products. */
			if (setsOfFeaturesTarget.contains(featureSetSource)) {
				/* This condition asks if the product source is also present in any product target. */ 
				/*It means, the set of features that compose the source product is also presented in any set of features that compose all target products.*/
				/* Trying to find a corresponding target product in a more economical way.  */
				System.out.println("\n\n\tCorrespondent Product");
				Product provavelCorrespondenteNoTarget = this.evaluateProductCKAM(featureSetSource, targetLine, id++);
				
				/* This Compares whether mapping between names and assets is the same in both products. */
				if (provavelCorrespondenteNoTarget.temMesmosAssetsEPreProcessConstants(productSource)) {
					 /* It will store the corresponding <TARGET> product. Corresponding products has the same features. Not necessarily the same assets and the same behavior. */
					productSource.setLikelyCorrespondingProduct(provavelCorrespondenteNoTarget);
				}else{
					/*It means that a generated product and its corresponding target product do not have the same assets.*/
					System.out.println("\nThe two products below do not have the same set of assets:");
					provavelCorrespondenteNoTarget.printSetOfFeatures();
					productSource.printSetOfFeatures();
				}
				
				/* It will add the new generated product in the set of products of the <TARGET> product line. */
				targetLine.getProducts().add(provavelCorrespondenteNoTarget);

				/* Remove the new already generated product from the set of Target product. */
				/* It will remain only configurations that do not exist in the original line. * It means, It will remain only products that is not correspondent to anyone from source line. */
				setsOfFeaturesTarget.remove(featureSetSource);
			}
			
		} /* FOR end*/

		/* This will generate the others products of the Target product Line.*/
		/* The products that did not have a corresponding source product line product. */
		for (HashSet<String> featureSetTarget : setsOfFeaturesTarget) {
			/* It will evaluate CK and AM in order to produce products of the SPL. */
			Product productTarget = this.evaluateProductCKAM(featureSetTarget, targetLine, id++);
			/* It will add the new generated product in the set of products of the <TARGET> product line. */
			targetLine.getProducts().add(productTarget);
		}

		/* Attempting to "marry" products that did not have a par. */
		System.out.println("\n\n ... Attempting to marry products that did not have a par.");
		for (Product productSource : sourceLine.getProducts()) {
			if (productSource.getLikelyCorrespondingProduct() == null) {
				System.out.println("\nThis product did not have a really correspondent target product:");
				String concat =  "";
				Iterator<String> i = productSource.getFeaturesList().iterator();
				while(i.hasNext()){
					String feature = (String) i.next();
					concat = concat + " [ " + feature + " ]";
				}
				System.out.println("\nProduct :: " + concat);
				/* It tries to catch a corresponding product in the target SPL. */
				Product provavelCorrespondenteNoTarget = this.getProvavelCorrespondenteNoTarget(productSource, targetLine.getProducts());
				if (provavelCorrespondenteNoTarget != null) {
					 /* It will store the corresponding <TARGET> product. Corresponding products has the same features. Not necessarily the same assets and the same behavior. */
					productSource.setLikelyCorrespondingProduct(provavelCorrespondenteNoTarget);
				} else {
					isRefinement = false;
				}
			}
		}

		System.out.println("\n\nAll products in the source has a really corresponding target product ?: " + isRefinement);
		System.out.println("\nIt means that a generated product and its corresponding target product may do not have the same assets.");
		return isRefinement;
	} /*Method end*/

	/**
	 * Walk through all Target products in order to find anyone who is correspondent to the source product.
	 * @param productSource
	 * @param productsTarget
	 * @return
	 */
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

	/**
	 * It will evaluate CK and AM in order to produce products of the SPL.
	 * @param featureSet A set of features that compose a product.
	 * @param productLine Source Product Line
	 * @param id Product Id
	 * @return This method returns a product.
	 * @throws IOException
	 * @throws AssetNotFoundException
	 */
	private Product evaluateProductCKAM(HashSet<String> featureSet, ProductLine productLine, int id) throws IOException, AssetNotFoundException {
		
		String concat =  "";
		Iterator<String> i = featureSet.iterator();
		while(i.hasNext()){
			String feature = (String) i.next();
			concat = concat + " [ " + feature + " ]";
		}
		System.out.println("\nProduct "+ (id) + " :: " + concat);
	
		/* <AssetName, path> Get in CK the DESTINY of the assets. */
 		HashMap<String, String> constantesDestinos = productLine.getCk().evalCKDestinos(featureSet);
		
		if(constantesDestinos.isEmpty()){
			System.out.println("constante destino esta vazio: " + constantesDestinos.isEmpty());
		}

		/* <AssetName, path> Get in CK the ORIGIN of the assets.*/
		HashMap<String, String> constantesOrigens = new HashMap<String, String>();

		for (String constant : constantesDestinos.keySet()) {
			/* Replacing invalid destinations, where source and destination are the same. */
			constantesDestinos.put(constant, constantesDestinos.get(constant) == null ? productLine.getAssetMapping().get(constant.trim()) : constantesDestinos.get(constant));

			/* The origin path is always the one informed in the asset mapping. */
			constantesOrigens.put(constant, productLine.getAssetMapping().get(constant.trim()));
		}
		
		HashSet<String> preProcessTags = this.builder.getPreProcessTags(featureSet);
		
		/*Constructs a new Product */
		return new Product(productLine, id, featureSet, preProcessTags, constantesOrigens, constantesDestinos);
	}

	/**
	 * This method checks if the SPL is well formed.	 <br></br>
	 * @param sourceLine SOURCE product line.  <br></br>
	 * @param targetLine TARGET product line.  <br></br>
	 * @return if the SPL is well formed.  <br></br>
	 */
	private boolean isWF(ProductLine sourceLine, ProductLine targetLine) {
		
		System.out.println("\nBuild the SOURCE Feature Model Alloy file:");
		this.buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
		
		System.out.println("\nBuild the TARGET Feature Model Alloy file:");
		this.buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);

		System.out.println("\nBuild the SOURCE Configuration Knowledge Alloy file:");
		this.buildAlloyCKFile(Constants.SOURCE_CK_ALLOY_NAME, this.sourceFMSemantics, "source", sourceLine);
		
		System.out.println("\nBuild the TARGET Configuration Knowledge Alloy file:");
		this.buildAlloyCKFile(Constants.TARGET_CK_ALLOY_NAME, this.targetFMSemantics, "target", targetLine);

	    /* Well Formedness to the <Source> SPL */
		SafeCompositionResult sourceComposition = checkSafeCompositionOfLine(Constants.SOURCE_CK_ALLOY_NAME, sourceLine.getFeatures(), "source");
		System.out.println("Well Formedness to the <Source> SPL: " + !sourceComposition.getAnalysisResult());

		/*Well Formedness to the <Target> SPL*/
		SafeCompositionResult targetComposition = checkSafeCompositionOfLine(Constants.TARGET_CK_ALLOY_NAME, targetLine.getFeatures(), "target");
		System.out.println("Well Formedness to the <Target> SPL.: " + !targetComposition.getAnalysisResult());

		return !sourceComposition.getAnalysisResult() && !targetComposition.getAnalysisResult();
		/* End of th Well Formedness Test */ 
	}

	/**
	 * Verifica se tanto mapeamento quanto conteudo das classes eh igual.
	 * 
	 * @return
	 */
	private boolean isAssetMappingEqual(ProductLine sourceLine, ProductLine targetLine) {
		boolean assetsEqual = false;
		assetsEqual = this.isSameAssets(sourceLine, targetLine);
		return assetsEqual;
	}

	private boolean testProducts(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach, Criteria criteria, ResultadoLPS resultado) throws IOException, DirectoryException {

		boolean isRefactoring = true;

		try {
			resultado.getMeasures().setQuantidadeProdutosCompilados(0);

			for (Product productSource : sourceLine.getProducts()) {
				if (approach == Approach.NAIVE_2_ICTAC || approach == Approach.NAIVE_1_APROXIMACAO || (approach == Approach.IMPACTED_FEATURES && productSource.containsSomeAsset(this.classesModificaadas, sourceLine.getMappingClassesSistemaDeArquivos()))) {
					
					this.builder.generateProduct(productSource, sourceLine.getPath(), resultado);

					Product provavelCorrespondente = productSource.getLikelyCorrespondingProduct();

					if (provavelCorrespondente != null) {
						this.builder.generateProduct(provavelCorrespondente, targetLine.getPath(), resultado);
						isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, provavelCorrespondente, sourceLine.getControladoresFachadas(), timeout, qtdTestes, approach, criteria, resultado);
					} else {
						/* If the source product does not have a correspondent target product it is NOT considered a refactoring.
						 * It means, that the behavior was not preserved once we can not find even a correspondent target product.*/
						isRefactoring = false;
					}

					if (approach != Approach.NAIVE_1_APROXIMACAO && approach != Approach.IMPACTED_FEATURES) {

						//Testa se o comportamento nao bate com nenhum outro destino. Exceto para o caso de NAIVE_WITHOUT_RENAMING.
						if (!isRefactoring) {
							for (Product productTarget : targetLine.getProducts()) {
								if (productTarget != provavelCorrespondente) {
									this.builder.generateProduct(productTarget, targetLine.getPath(), resultado);

									isRefactoring = CommandLine.isRefactoring(productSource, productTarget, sourceLine.getControladoresFachadas(), timeout, qtdTestes, approach, criteria, resultado);

									//Para de procurar se encontrar um par com mesmo comportamento.
									if (isRefactoring) {
										break;
									}
								}
							}
						}
					}

					/* If one method is not a refactoring, we can break this loop and let the user know about the refactoring was not applied successfully. */
					if (!isRefactoring) {
						break;
					}
				}
			}
		} catch (AssetNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("This evolution is a refactoring ?: " + "< " + isRefactoring + " >");

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

	/**
	 * Asks if the asset mapping is a refinement .	 <br></br>
	 * @param sourceLine
	 * @param targetLine
	 * @param timeout
	 * @param qtdTestes
	 * @param approach
	 * @param changedFeatures
	 * @param criteria
	 * @param resultado
	 * @return
	 * @throws IOException
	 */
	private boolean isAssetMappingRefinement(ProductLine sourceLine, ProductLine targetLine, int timeout, int qtdTestes, Approach approach, HashSet<String> changedFeatures, Criteria criteria, ResultadoLPS resultado) throws IOException {
		boolean ehAssetMappingRefinement = false;
		try {
			ehAssetMappingRefinement = this.checkAssetMappingBehavior(sourceLine, targetLine, timeout, qtdTestes, approach, changedFeatures, criteria, resultado);
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
			e.printStackTrace();
		}
		long finishedTime = System.currentTimeMillis();
		FeatureModelEvolutionResult fmResult = new FeatureModelEvolutionResult(startedTime, finishedTime, fmRefactoring);
		return fmResult;
	}

	/**
	 * This method checks the Safe Composition of the SPL.	 <br></br>
	 * @param string
	 * @param features
	 * @param name
	 * @return returns a SafeCompositionResult
	 * @see SafeCompositionResult
	 */
	private SafeCompositionResult checkSafeCompositionOfLine(String string, HashSet<String> features, String name) {
		System.out.println("\n\n\t\tThe beginning of the safe composition test.\n");
		SafeCompositionResult checkCKSource = null;
		try {
			checkCKSource = SafeCompositionVerifier.checkCK(Constants.ALLOY_PATH, string, Constants.ALLOY_EXTENSION, string + Constants.ALLOY_EXTENSION, features, name);
		} catch (Err e) {
			System.out.println("\nAn Error Occurred when trying to do Safe Composition Test.\n\n" + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("\n\t\tEnd of Safe Composition test.\n");
		return checkCKSource;
	}

	private void buildFMEvolutionAlloyFile(String sourceFMXML, String targetFMXML) {
		AlloyFMEvolutionBuilder evolutionAlloy = new AlloyFMEvolutionBuilder();
		evolutionAlloy.buildAlloyFile("evolution", Constants.ALLOY_PATH + Constants.EVOLUTION_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, "source", sourceFMXML, "target", targetFMXML);
	}

	/**
	 * This Method builds FM Alloy FIle <br></br>
	 * @param moduleName
	 * @param sourceFmAlloyName
	 * @param productLine
	 */
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
			correctSet.add(string.trim());
		}
		String header = "module " + name + Constants.LINE_SEPARATOR;

		String sigs = "one sig ";
		String separador = "";
		for (String string : correctSet) {
			string = string.trim();
			if (string != null && !string.equals("")) {
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

	/**
	 * This methods answers whether the assets of the SOURCE product line is completely equal to the TARGET product line. 
	 * It means, that it looks for the modified assets.  <br></br>
	 * @param sourceLine  <br></br>
	 * @param targetLine  <br></br>
	 * @return returns is both product lines have the same assets. Without changes.
	 */
	public boolean isSameAssets(ProductLine sourceLine, ProductLine targetLine) {
		boolean result = true;
		
		/* Get all SOURCE product line classes. */
		Set<String> sourceKeySet = sourceLine.getMappingClassesSistemaDeArquivos().keySet();
		
		/* Get all TARGET product line classes. */
		Set<String> targetKeySet = targetLine.getMappingClassesSistemaDeArquivos().keySet();

		/* Initialize the modified classes variable. */
		this.classesModificaadas = new HashSet<String>();
		
		/* Initialize the changed assets variable. */
		this.changedAssets = new HashSet<String>();

		/* walk through all assets of the SOURCE product line classes. */
		for (String asset : sourceKeySet) {
			/* location source file*/
			String locationSource = sourceLine.getMappingClassesSistemaDeArquivos().get(asset);
			/* location target file*/
			String locationTarget = targetLine.getMappingClassesSistemaDeArquivos().get(asset);
			if (locationSource != null && locationTarget != null) {
				/* build the two files.*/
				File sourceFile = new File(locationSource); // source asset File.
				File targetFile = new File(locationTarget); // target asset File.
				try {
					boolean equals;
					if (asset.endsWith("java")) {
						/* This methods compares two textual files and return whether they are equal.
						 * It means, there is no refactoring in the second when compared to the first one. */
						this.astComparator.setInputs(sourceFile, targetFile);
						equals = this.astComparator.isIsomorphic();
					} else if (asset.endsWith("aj")) {
						/* This methods compares two textual files and return whether they are equal. 
						 * It means, there is no refactoring in the second when compared to the first one. */
						equals = this.isTextualFileContentsEquals(sourceFile, targetFile);
					} else {
						equals = true;
					}
					if (!equals) {
						result = false;
						/*Put the asset in the modified classes.*/
						this.classesModificaadas.add(asset);
						this.changedAssets.add(this.filesManager.getPath("src." + asset));
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!targetKeySet.containsAll((sourceKeySet))) {
			result = false;
		}

		return result;
	}

	/**
	 * This methods compares two textual files and return whether they are equal. 
	 * It means, there is no refactoring in the second when compared to the first one.
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 */
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

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


	/**
	 * Checa apenas classes modificadas. Se mais de MAX_CLASSES_MODIFICADAS
	 * forem alteradas, a otimizacao eh descartada. This methods also is responsible to answer whether the evolution is a refactoring or not.
	 * It means, is the evolution preserving behavior ?  <br></br>
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
	private boolean checkAssetMappingBehavior(ProductLine sourceLine, ProductLine targetLine, int timeout, int maxTests, Approach approach, HashSet<String> changedFeatures, Criteria criteria, ResultadoLPS resultado) throws IOException, AssetNotFoundException, DirectoryException {
		boolean sameBehavior = false;
		long startedTime = System.currentTimeMillis();

		/* The amount of modified classes are greater than MAX_CLASSES_MODIFICADAS */
		/* The optimization for AM won't be realized. */
		if (this.classesModificaadas.size() > MAX_CLASSES_MODIFICADAS) {
			System.out.println("\n\n ! Warning: The amount of modified classes are greater than" + MAX_CLASSES_MODIFICADAS);
			System.out.println("The optimization for AM won't be realized.\n");
		} else {
			/* Set the amout of compiled products to 1*/
			resultado.getMeasures().setQuantidadeProdutosCompilados(1);

			String productPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR;

			/* Creating folders for source and target products, which will be the same for all modified classes and their dependencies. */
			File testSourceDirectory = FilesManager.getInstance().createDir( productPath + "source" + Constants.FILE_SEPARATOR + (line == Lines.TARGET ? "src" : ProductBuilder.SRCPREPROCESS));
			File testTargetDirectory = FilesManager.getInstance().createDir( productPath + "target" + Constants.FILE_SEPARATOR + (line == Lines.TARGET ? "src" : ProductBuilder.SRCPREPROCESS));

			/* This creates bin folder for SOURCE PRODUCT and TARGET PRODUCT. */
			FilesManager.getInstance().createDir(productPath + "source" + Constants.FILE_SEPARATOR + "bin");
			FilesManager.getInstance().createDir(productPath + "target" + Constants.FILE_SEPARATOR + "bin");

			/* Get library path of the SOURCE and TARGET SPL*/
			String libPathSource = sourceLine.getLibPath();
			String libPathtarget = targetLine.getLibPath();

			if (libPathSource != null && libPathtarget != null) {
				/* This copies all library files for SOURCE PRODUCT and TARGET PRODUCT lib path. */
				FilesManager.getInstance().copyLibs(libPathSource, productPath + "source" + Constants.FILE_SEPARATOR + "lib");
				FilesManager.getInstance().copyLibs(libPathtarget, productPath + "target" + Constants.FILE_SEPARATOR + "lib");
			}

			/* Classes that will have generated tests. */
			String classes = "";
			String classeToGenerateTestes = "";

			this.listaAspectos = new HashSet<String>();

			System.out.println("\n Amount of modified classes: " + classesModificaadas.size()+"\n");
			/* walk through all changed classes. */
			for (String classe : this.classesModificaadas) {
				System.out.println(" - Modified: " + classe);

				/* Get the whole path of the modified class. */
				String fileSourcePath = sourceLine.getMappingClassesSistemaDeArquivos().get(classe);
				String fileTargetPath = targetLine.getMappingClassesSistemaDeArquivos().get(classe);

				/*Copying source version of the modified file. */
				String destinationPath = null;
				File fileDestination = null;
				destinationPath = testSourceDirectory.getAbsolutePath() + FilesManager.getInstance().getPathAPartirDoSrc(fileSourcePath).replaceFirst("src", "");
				fileDestination = new File(destinationPath);
				classeToGenerateTestes = classe;

				/*is the SPL -> TARGET SOFTWARE PRODUCT LINE ?*/
				if (this.line == Lines.TARGET){
					classeToGenerateTestes = FilesManager.getInstance().getPathAPartirDoSrc(classeToGenerateTestes).replaceFirst(Pattern.quote("src.java."), "");
				}
				FilesManager.getInstance().createDir(fileDestination.getParent());
				FilesManager.getInstance().copyFile(fileSourcePath, fileDestination.getAbsolutePath());

				/* Catch the class path in source version. */
				File fileSource = fileDestination;
				destinationPath = testTargetDirectory.getAbsolutePath()	+ FilesManager.getInstance().getPathAPartirDoSrc(fileTargetPath).replaceFirst("src", "");
				fileDestination = new File(destinationPath);
				FilesManager.getInstance().createDir(fileDestination.getParent());
				FilesManager.getInstance().copyFile(fileTargetPath, fileDestination.getAbsolutePath());

				/* Catch the class path in source version. */
				File fileTarget = fileDestination;

				this.dependenciasCopiadas = new HashSet<String>();

				this.copyDependencies(fileSource, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(), null, sourceLine.getDependencias());
				this.copyDependencies(fileTarget, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), null, targetLine.getDependencias());

				/* As aspects are not preprocessed, it will be necessary to manually move the aspects to the src folder after the preprocessing. */
				if (fileSource.getAbsolutePath().endsWith("aj")) {
					this.listaAspectos.add(fileSource.getAbsolutePath());
				}
				if (fileTarget.getAbsolutePath().endsWith("aj")) {
					this.listaAspectos.add(fileTarget.getAbsolutePath());
				}
				if (classes.equals("")){
					classes = classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
				} else if (!classes.contains(classeToGenerateTestes.replaceAll(".java", ""))) {
					classes = classes + "|" + classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
				}
			}

			/* Check only products that have any of the modified classes. */
			/* This variable will store modified products. It means, products that have at least one modified asset. */ 
			ArrayList<Product> produtosQueContemClassesModificadas = new ArrayList<Product>();
			
			for (Product product : sourceLine.getProducts()){
				/* Is this product contains at least one modified asset ? */
				if (product.containsSomeAsset(this.classesModificaadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
					/* if the answer is YES, add this product to the modified products variable. */
					produtosQueContemClassesModificadas.add(product);
				}
			}

			//Se tiver aspectos ou tags de pre-processamento no codigo, faz um for com todos os produtos possiveis.
			//Filtrar entre os produtos poss�veis, removendo os que tiverem mesmos conjuntos de aspectos que interferem

			//		**Eh melhor entrar aqui se temAspectos, mesmo que nao tenha preprocess
			//		**Quando tiver soh preprocess entra no else
			//		**
			
			/* Does SOURCE product line contains aspectos ? */
			if (sourceLine.temAspectos()) {
				HashSet<HashSet<String>> products = new HashSet<HashSet<String>>();
				
				/* Removes all of the elements from this list. */
				produtosQueContemClassesModificadas.clear();

				for (Product product : sourceLine.getProducts()) {
					/* Is this product contains at least one modified asset ? */
					if (product.containsSomeAsset(this.classesModificaadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
						produtosQueContemClassesModificadas.add(product);
						products.add(product.getFeaturesList());
					}
				}

				/* if it has aspects, but there are no classes with more than one version in the SPL. */
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

				System.out.println("\n products size: " + pseudoProductsToBePreprocessed.size());
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
							FilesManager.getInstance().copyFile(sourceLine.getMappingClassesSistemaDeArquivos().get(FilesManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());
							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());
							this.copyDependencies(fileDestination, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(), filesToTrash, sourceLine.getDependencias());
						}
					}

					for (String aspecto : aspectosDaConfiguracaoTarget) {
						String destinationPath = testTargetDirectory.getAbsolutePath() + aspecto.split("src")[1];

						if (!this.classesModificaadas.contains(aspecto) && !this.listaAspectos.contains(aspecto)) {
							File fileDestination = new File(destinationPath);

							FilesManager.getInstance().createDir(fileDestination.getParent());
							FilesManager.getInstance().copyFile(targetLine.getMappingClassesSistemaDeArquivos().get( FilesManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());

							this.copyDependencies(fileDestination, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), filesToTrash, targetLine.getDependencias());
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

					sameBehavior = sameBehavior && CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(), classes, timeout, maxTests, approach, criteria, sourceLine.getPath(), targetLine.getPath(), resultado, false, false);
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

	private void copyDependencies(File classe, File destinationDirectory, HashMap<String, String> mapping, ArrayList<File> filesToTrash, HashMap<String, Collection<String>> dependenciasCache) throws AssetNotFoundException, DirectoryException {

		String pathDependencia = FilesManager.getInstance().getPathAPartirDoSrc( classe.getAbsolutePath().replaceFirst("srcpreprocess", "src"));

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
	 * This method creates a representation of the source and target product line. 
	 * In addition, set the libraries path for the target and source product lines.
	 * 
	 * Note: This name is not fair enough. It is necessary to apply a refactoring.
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

		/*This part creates a representation of the source product line.*/
		ProductLine sourceLine = new ProductLine(sourcePath, sourcePath + "/ck.xml", sourcePath + "/fm.xml", sourcePath + "/am.txt", temAspectosSource, controladoresFachadas, sourceCKKind, sourceAMFormat);

		/*Set the libraries path for the source product line.*/
		sourceLine.setLibPath(libPathSource);
		
		/*This part creates a representation of the target product line.*/
		ProductLine targetLine = new ProductLine(targetPath, targetPath + "/ck.xml", targetPath + "/fm.xml", targetPath + "/am.txt", temAspectosTarget, controladoresFachadas, targetCKKind, targetAMFormat);
		
		/*Set the libraries path for the target product line.*/
		targetLine.setLibPath(libPathTarget);

		/* Verify the SPL with the created source and target product line.*/
		return this.verifyLine(sourceLine, targetLine, timeout, qtdTestes, selectedApproaches, criteria, resultado);
	}

}
