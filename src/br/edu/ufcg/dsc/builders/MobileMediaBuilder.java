package br.edu.ufcg.dsc.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

public class MobileMediaBuilder extends ProductBuilder {


//	public static final String DEFAULT_LINE = "preprocessor.symbols";
//	public static final String SOURCE_FILES_LOCATION = "midp-preprocessed";
//	public static final String BINARY_FILES_LOCATION = "midp-compiled";
	
	public MobileMediaBuilder() {
		super();
		
		this.createConstants();
	}
	
	private void createConstants() {
		this.preprocessFeaturesToConstants = new HashMap<String, String>();
		
		this.preprocessFeaturesToConstants.put("sorting", "includeSorting");
		this.preprocessFeaturesToConstants.put("s176x205", "device_screen_176x205");
		this.preprocessFeaturesToConstants.put("s128x149", "device_screen_128x149");
		this.preprocessFeaturesToConstants.put("s132x176", "device_screen_132x176");
		this.preprocessFeaturesToConstants.put("favourites", "includeFavourites");
		this.preprocessFeaturesToConstants.put("copyphoto", "includeCopyPhoto");
		this.preprocessFeaturesToConstants.put("smsfeature", "includeSmsFeature");
		
		this.preprocessConstantsToFeatures = new HashMap<String, String>();
		
		for(String feature : this.preprocessFeaturesToConstants.keySet()){
			this.preprocessConstantsToFeatures.put(this.preprocessFeaturesToConstants.get(feature), feature);
		}
	}

//	public void generateProducts(String pathToSourceAlloyFM, String sourcePath,
//			String targetPath, String sourceAM, String targetAM,
//			String sourceCKXML, String targetCKXML,
//			HashSet<String> changedFeatures, HashMap<String, HashSet<HashSet<String>>> cacheProductsSource) throws IOException,
//			AssetNotFoundException {
//		
//		this.createDataStructures();
//
//		//  Tem algum bug aqui. Produtos repetidos sao criados durante a geracao e ela eh muito custosa...
//		//	Por enquanto os produtos serao todos armazenados em cache antes de comecar a execucao.
//		//	HashSet<HashSet<String>> products = this.getProductsFromAlloy(pathToSourceAlloyFM);
//
//		HashSet<HashSet<String>> products = cacheProductsSource.get(sourcePath);
//
//		if (changedFeatures != null) {
//			products = filter(products, changedFeatures);
//		}
//
//		Measures.getInstance().setQuantidadeProdutosCompilados(products.size());
//
//		System.out.println("MEUS PRODUTOS = " + products);
//
//		int counter = 0;
//		ConfigurationKnowledge sourceCK = XMLReader.getCK(sourceCKXML, sourceAM);
//		ConfigurationKnowledge targetCK = XMLReader.getCK(targetCKXML, targetAM);
//
//		for (HashSet<String> features : products) {
//			ArrayList<String> sourceAssets = new ArrayList<String>();
//			ArrayList<String> sourceDestinos = new ArrayList<String>();
//			
//			//Constante -> Destino
//			HashMap<String,String> evalCKSource = sourceCK.evalCKDestinos(features);
//			
//			for (String constant : evalCKSource.keySet()) {
//				//Considerando que o mapeamento seja se 1:1.
//				sourceAssets.add(this.filesManager.getAssets(constant.trim(), sourceAM).get(0));
//				sourceDestinos.add(
//						evalCKSource.get(constant)==null ? 
//								this.filesManager.getAssets(constant.trim(), sourceAM).get(0) : 
//									evalCKSource.get(constant));
//			}
//
//			ArrayList<String> targetAssets = new ArrayList<String>();
//			ArrayList<String> targetDestinos = new ArrayList<String>();
//			
//			HashMap<String,String> evalCKTarget = targetCK.evalCKDestinos(features);
//			
//			for (String constant : evalCKTarget.keySet()) {
//				//Considerando que o mapeamento seja se 1:1.
//				targetAssets.add(this.filesManager.getAssets(constant.trim(), targetAM).get(0));
//				targetDestinos.add(evalCKTarget.get(constant) == null ?
//						this.filesManager.getAssets(constant.trim(), targetAM).get(0) : 
//							evalCKTarget.get(constant));
//			}
//			
//			createDirs(String.valueOf(counter), this.generateFilesToPreProcess(sourceDestinos), this.generateFilesToPreProcess(targetDestinos),
//					sourcePath, targetPath);
//			
//			this.filesManager.copyFiles(sourcePath, sourceAssets, this.generateFilesToPreProcess(sourceDestinos), productPath);
//			this.filesManager.copyFiles(targetPath, targetAssets, this.generateFilesToPreProcess(targetDestinos), targetProductsPath);
//			
//			System.out.println("PATH = " + productPath + " E = "
//					+ targetProductsPath);
//			
//			Product currentProduct = new Product(counter, features, 0, 0);
//			updtadeProductsList(currentProduct);
//			counter++;
//			
//			this.preprocess(this.getSymbols(features), currentProduct.getSourceDir());
//			this.preprocess(this.getSymbols(features), currentProduct.getTargetDir());
//		}
//	}

	private ArrayList<String> generateFilesToPreProcess(Collection<String> assets) {
		ArrayList<String> result = new ArrayList<String>();
		
		for(String asset : assets){
			result.add(asset.replaceFirst("src", SRCPREPROCESS));
		}
		
		return result;
	}
	
//	public void generateProducts(String pathToSourceAlloyFM, String sourcePath,
// 			String targetPath, String sourceAM, String targetAM, String sourceCKXML, String targetCKXML, HashSet<String> changedFeatures) throws IOException, AssetNotFoundException {
//
//		System.out.println("GERAR OS PRODUTOS");
//		long startedGeneration = System.currentTimeMillis();
//		HashSet<HashSet<String>> productsFromAlloy = super
//				.getProductsFromAlloy(pathToSourceAlloyFM);
//		long finishedGeneration = System.currentTimeMillis();
//		
//		System.out.println("OS PRODUTOS " + productsFromAlloy);
//		int counter = 0;
//		for (HashSet<String> features : productsFromAlloy) {
//			filesManager.createDir(FILES_DIR);
//			String temp = "Product" + counter;
//			filesManager.createDir(FILES_DIR + Constants.FILE_SEPARATOR + temp);
//
//			sourceProductsPath = FILES_DIR + Constants.FILE_SEPARATOR + temp
//					+ Constants.FILE_SEPARATOR + "source";
//			targetProductsPath = FILES_DIR + Constants.FILE_SEPARATOR + temp
//					+ Constants.FILE_SEPARATOR + "target";
//			defaultSourcePath = sourcePath;
//			filesManager.createDir(sourceProductsPath);
////			filesManager.createDir(sourceProductsPath
////					+ Constants.FILE_SEPARATOR + "src");
////			filesManager.createDir(sourceProductsPath
////					+ Constants.FILE_SEPARATOR + "bin");
//
//			filesManager.createDir(targetProductsPath);
////			filesManager.createDir(targetProductsPath
////					+ Constants.FILE_SEPARATOR + "src");
////			filesManager.createDir(targetProductsPath
////					+ Constants.FILE_SEPARATOR + "bin");
//			// createDir(targetProductsPath + Constants.FILE_SEPARATOR + "lib");
//
//			defaultTargetPath = targetPath;
//			try {
//				buildBasicStructure(sourcePath, sourceProductsPath);
//				buildBasicStructure(targetPath, targetProductsPath);
//			} catch (DirectoryException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			buildLaBuildFile(features, sourcePath + Constants.FILE_SEPARATOR
//					+ LABUILD, sourceProductsPath + Constants.FILE_SEPARATOR
//					+ LABUILD);
//			buildLaBuildFile(features, targetPath + Constants.FILE_SEPARATOR
//					+ LABUILD, targetProductsPath + Constants.FILE_SEPARATOR
//					+ LABUILD);
//			compileProducts(sourceProductsPath, targetProductsPath);
//
//			try {
//				copyFiles(sourceProductsPath);
//				copyFiles(targetProductsPath);
//			} catch (DirectoryException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Product currentProduct = new Product(counter, features,
//					executeBuildSource, executeBuildTarget);
//			updtadeProductsList(currentProduct);
//			counter++;
//		}
//	}

//	private void copyFiles(String sourcePath) throws AssetNotFoundException,
//			DirectoryException {
//		System.out.println("COPIAR ARQUIVOS");
//		buildBasicStructure(sourcePath + Constants.FILE_SEPARATOR
//				+ SOURCE_FILES_LOCATION, sourcePath);
//		buildBasicStructure(sourcePath + Constants.FILE_SEPARATOR
//				+ BINARY_FILES_LOCATION, sourcePath + Constants.FILE_SEPARATOR
//				+ "bin");
//	}

//	public void compileProducts(String sourcePath, String targetPath) {
//		executeBuildSource = executeBuild(sourcePath);
//		executeBuildTarget = executeBuild(targetPath);
//	}

//	private long executeBuild(String sourcePath) {
//
//		System.out.println("Executar build em " + sourcePath);
//		File buildFile = new File(sourcePath + Constants.FILE_SEPARATOR
//				+ "build.xml");
//		Project p = new Project();
//		// p.setUserProperty("ant.file", buildFile.getAbsolutePath());
//		// p.setProperty("source", sourcePath);
//		// p.setUserProperty("ant/build.properties",
//		// buildFile.getAbsolutePath());
//		// p.setProperty("target", targetPath);
//
//		// p.setProperty("tests.folder", Constants.TEST);
//
//		DefaultLogger consoleLogger = new DefaultLogger();
//		consoleLogger.setErrorPrintStream(System.err);
//		consoleLogger.setOutputPrintStream(System.out);
//		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
//		p.addBuildListener(consoleLogger);
//
//		p.init();
//		ProjectHelper helper = ProjectHelper.getProjectHelper();
//		p.addReference("ant.projectHelper", helper);
//		helper.parse(p, buildFile);
//
//		long startedTime = System.currentTimeMillis();
//		p.executeTarget("generate-compile");
//		long finishedTime = System.currentTimeMillis();
//		p.executeTarget("full-build");
//		filesManager.cleanDir(sourcePath + Constants.FILE_SEPARATOR + "src");
//		filesManager.cleanDir(sourcePath + Constants.FILE_SEPARATOR + "bin");
//		return finishedTime - startedTime;
//	}

//	private void buildLaBuildFile(HashSet<String> features,
//			String sourceLaBuild, String targetLaBuild) {
//		System.out.println("VOU CRIAR O LABUILD! de " + sourceLaBuild
//				+ " para " + targetLaBuild);
//		ArrayList<String> fileContent = filesManager
//				.getFileContent(sourceLaBuild);
//		ArrayList<String> out = getContent(features, fileContent);
//		filesManager.createFile(sourceProductsPath + Constants.FILE_SEPARATOR
//				+ LABUILD, out);
//		ArrayList<String> targetContent = filesManager
//				.getFileContent(targetLaBuild);
//		ArrayList<String> out2 = getContent(features, targetContent);
//		filesManager.createFile(targetProductsPath + Constants.FILE_SEPARATOR
//				+ LABUILD, out2);
//		System.out.println("FIM DO LABUILD!!");
//	}

//	private ArrayList<String> getContent(HashSet<String> features,
//			ArrayList<String> fileContent) {
//		ArrayList<String> out = new ArrayList<String>();
//
//		for (String string : fileContent) {
//			if (!string.startsWith(DEFAULT_LINE)) {
//				out.add(string);
//			} else {
//				String correct = DEFAULT_LINE + "=device_screen_132x176";
//				String separator = ", include";
//				for (String feature : features) {
//					if (!feature.trim().equals("")
//							&& !feature.equals("MobileMedia")) {
//						correct += separator + feature;
//					}
//				}
//				out.add(correct);
//			}
//		}
//		return out;
//	}

	
	//Funciona com pr�-processamento do Antenna.
	//MobileMedia entra aqui.
	public void generateProduct(Product product, String pathSPL, ResultadoLPS resultado) throws AssetNotFoundException, DirectoryException {
		if(!product.isGenerated()){
			ArrayList<String> assetsOrigens = new ArrayList<String>();
			ArrayList<String> assetsDestinos = new ArrayList<String>();
			
			product.sortAssetNames(assetsOrigens, assetsDestinos);
			
			ArrayList<String> filesToPreProcess = this.generateFilesToPreProcess(assetsDestinos);
			
			this.createDirs(product, filesToPreProcess, pathSPL);
			this.filesManager.copyFiles(pathSPL, assetsOrigens, filesToPreProcess, product.getPath());

			this.preprocess(this.generateStringPreProcessTags(product.getPreProcessTags()), product.getPath(), resultado);
			
			//Aspectos precisam ser copiados manualmente quando o preprocessador utilizado eh o Antenna.
		//	for()
			String libPath = product.getSpl().getLibPath();
			
			if(libPath != null){
				FilesManager.getInstance().copyLibs(libPath, product.getPath() + File.separator + "lib");
			}
			
			product.setGenerated(true);
			
			resultado.getMeasures().setQuantidadeProdutosCompilados(resultado.getMeasures().getQuantidadeProdutosCompilados() + 1);
		}
	}
}
