package br.edu.ufcg.dsc.builders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

public class TargetBuilder extends ProductBuilder {


//	public static final String DEFAULT_LINE = "preprocessor.symbols";
//	public static final String SOURCE_FILES_LOCATION = "midp-preprocessed";
//	public static final String BINARY_FILES_LOCATION = "midp-compiled";
	
	public TargetBuilder() {
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
		
		this.preprocessFeaturesToConstants.put("cm", "cm");
		this.preprocessFeaturesToConstants.put("input", "input");
		this.preprocessFeaturesToConstants.put("mswordinput", "mswordinput");
		this.preprocessFeaturesToConstants.put("std", "std");
		this.preprocessFeaturesToConstants.put("xmlinput", "xmlinput");
		this.preprocessFeaturesToConstants.put("language", "language");
		this.preprocessFeaturesToConstants.put("enus", "enus");
		this.preprocessFeaturesToConstants.put("ptbr", "ptbr");
		this.preprocessFeaturesToConstants.put("output", "output");
		this.preprocessFeaturesToConstants.put("html", "html");
		this.preprocessFeaturesToConstants.put("tc3", "tc3");
		this.preprocessFeaturesToConstants.put("tc4", "tc4");
		this.preprocessFeaturesToConstants.put("importtemplate", "importtemplate");
		this.preprocessFeaturesToConstants.put("xmloutput", "xmloutput");
		this.preprocessFeaturesToConstants.put("stdoutput", "stdoutput");
		this.preprocessFeaturesToConstants.put("simplexmloutput", "simplexmloutput");

		this.preprocessConstantsToFeatures = new HashMap<String, String>();
		
		for(String feature : this.preprocessFeaturesToConstants.keySet()){
			this.preprocessConstantsToFeatures.put(this.preprocessFeaturesToConstants.get(feature), feature);
		}
	}

	@Override
	public void generateProduct(Product product, String pathSPL, ResultadoLPS resultado)
			throws AssetNotFoundException, IOException, DirectoryException {
		
		if(!product.isGenerated()){
			ArrayList<String> assetsOrigens = new ArrayList<String>();
			ArrayList<String> assetsDestinos = new ArrayList<String>();
			
			product.sortAssetNames(assetsOrigens, assetsDestinos);
			
			this.createDirs(product, assetsDestinos, pathSPL);
			
			this.filesManager.copyFilesDirectory(pathSPL, assetsOrigens, assetsDestinos, product.getPath(), true);
			
			
			File productDirectory = new File(product.getPath());
			
			this.preprocessVelocity(product.getFeaturesList(), productDirectory, 
					product.getSpl(), product.getPath(), resultado);
			
			String libPath = product.getSpl().getLibPath();
			
			if(libPath != null){
				FilesManager.getInstance().copyLibs(libPath, product.getPath() + File.separator + "lib");
			}
			
			product.setGenerated(true);
			resultado.getMeasures().setQuantidadeProdutosCompilados(resultado.getMeasures().getQuantidadeProdutosCompilados() + 1);
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
//			ArrayList<String> sourceAssetsAspects = new ArrayList<String>();
//			ArrayList<String> sourceDestinosAspects = new ArrayList<String>();
//			
//			//Constante -> Destino
//			HashMap<String,String> evalCKSource = sourceCK.evalCKDestinos(features);
//			
//			for (String constant : evalCKSource.keySet()) {
//				//Considerando que o mapeamento seja se 1:1.
//				String asset = this.filesManager.getAssets(constant.trim(), sourceAM).get(0);
//
//				if(asset.endsWith(".aj")){
//					sourceAssetsAspects.add(asset);
//				}
//				else{
//					sourceAssets.add(asset);
//				}
//
//				asset = evalCKSource.get(constant)==null ? this.filesManager.getAssets(constant.trim(), sourceAM).get(0) : evalCKSource.get(constant);
//
//				if(asset.endsWith(".aj")){
//					sourceDestinosAspects.add(asset);
//				}
//				else{
//					sourceDestinos.add(asset);
//				}
//			}
//
//			ArrayList<String> targetAssets = new ArrayList<String>();
//			ArrayList<String> targetDestinos = new ArrayList<String>();
//			
//			ArrayList<String> targetAssetsAspects = new ArrayList<String>();
//			ArrayList<String> targetDestinosAspects = new ArrayList<String>();
//			
//			HashMap<String,String> evalCKTarget = targetCK.evalCKDestinos(features);
//			
//			for (String constant : evalCKTarget.keySet()) {
//				String asset = this.filesManager.getAssets(constant.trim(), targetAM).get(0);
//				
//				if(asset.endsWith(".aj")){
//					targetAssetsAspects.add(asset);
//				}
//				else{
//					targetAssets.add(asset);
//				}
//
//				asset = evalCKTarget.get(constant)==null ? this.filesManager.getAssets(constant.trim(), targetAM).get(0) : evalCKTarget.get(constant);
//
//				if(asset.endsWith(".aj")){
//					targetDestinosAspects.add(asset);
//				}
//				else{
//					targetDestinos.add(asset);
//				}
//			}
//			
//			createDirs(String.valueOf(counter), sourceDestinos, targetDestinos, sourcePath, targetPath);
//			
//			ArrayList<String> sourceToPreprocess = this.filesManager.copyFilesDirectory(sourcePath, sourceAssets, sourceDestinos, productPath);
//			this.filesManager.limparArquivosJava();
//			
//			for(String file : sourceAssetsAspects){
//				sourceToPreprocess.add(this.productPath + "/" + file);
//			}
//			
//			ArrayList<String> targetToPreprocess = this.filesManager.copyFilesDirectory(targetPath, targetAssets, targetDestinos, targetProductsPath);
//			this.filesManager.limparArquivosJava();
//			
//			for(String file : targetAssetsAspects){
//				targetToPreprocess.add(this.targetProductsPath + "/" + file);
//			}
//			
//			//Aspectos ainda sao copiados da mesma forma que os Assets do MobileMedia.
//			//Como o pre processamento da Target eh feito com o Velocity, origem e destinos dos assets eh o mesmo.
//			this.filesManager.copyFiles(sourcePath, sourceAssetsAspects, sourceDestinosAspects, productPath);
//			this.filesManager.copyFiles(targetPath, targetAssetsAspects, targetDestinosAspects, targetProductsPath);
//			
//			System.out.println("PATH = " + productPath + " E = "
//					+ targetProductsPath);
//			
//			Product currentProduct = new Product(counter, features, 0, 0);
//			updtadeProductsList(currentProduct);
//			counter++;
//			
//			this.preprocessVelocity(features, sourceToPreprocess);
//			this.preprocessVelocity(features, targetToPreprocess);
//		}
//	}
}
	