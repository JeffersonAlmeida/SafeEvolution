package br.edu.ufcg.dsc.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

public class MobileMediaBuilder extends ProductBuilder {
	
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

	/**
	 * This method generates files to be processed.
	 * @param assets A collection(String) of assets
	 * @return
	 */
	private ArrayList<String> generateFilesToPreProcess(Collection<String> assets) {
		ArrayList<String> result = new ArrayList<String>();
		
		for(String asset : assets){
			result.add(asset.replaceFirst("src", SRCPREPROCESS));
		}
		
		return result;
	}
	

	/**
	 * This Method works properly to generate <strong>Mobile Media</strong> Products with Antenna preprocessor.
	 * @see Lines
	 */
	public void generateProduct(Product product, String pathSPL, ResultadoLPS resultado) throws AssetNotFoundException, DirectoryException {
		if(!product.isGenerated()){

			ArrayList<String> assetsOrigens = new ArrayList<String>();
			
			ArrayList<String> assetsDestinos = new ArrayList<String>();
			
			product.sortAssetNames(assetsOrigens, assetsDestinos);
			
			ArrayList<String> filesToPreProcess = this.generateFilesToPreProcess(assetsDestinos);
			
			this.createDirs(product, filesToPreProcess, pathSPL);
			this.filesManager.copyFiles(pathSPL, assetsOrigens, filesToPreProcess, product.getPath());

			this.preprocess(this.generateStringPreProcessTags(product.getPreProcessTags()), product.getPath(), resultado);
			
			/*Aspectos precisam ser copiados manualmente quando o preprocessador utilizado eh o Antenna. */
			String libPath = product.getSpl().getLibPath();
			
			if(libPath != null){
				FilesManager.getInstance().copyLibs(libPath, product.getPath() + File.separator + "lib");
			}
			
			product.setGenerated(true);
			
			resultado.getMeasures().setQuantidadeProdutosCompilados(resultado.getMeasures().getQuantidadeProdutosCompilados() + 1);
		}
	}
}
