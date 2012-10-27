package br.edu.ufcg.dsc.builders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

public class TargetBuilder extends ProductBuilder {

	 private static TargetBuilder instance = null; 

	 public static TargetBuilder getInstance(){  
        if (instance == null){  
            instance = new TargetBuilder();  
        }  
        return instance;  
	 }  
	
	private TargetBuilder() {
		super();
		this.createConstants("101SPL");
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
	
	private void createConstants(String s) {
		
		this.preprocessFeaturesToConstants = new HashMap<String, String>();
		
		this.preprocessFeaturesToConstants.put("JavaExorcismSPL", "JavaExorcismSPL");
		this.preprocessFeaturesToConstants.put("TreeStructure", "TreeStructure");
		this.preprocessFeaturesToConstants.put("Logging", "Logging");
		this.preprocessFeaturesToConstants.put("AccessControl", "AccessControl");
		this.preprocessFeaturesToConstants.put("Cut", "Cut");
		this.preprocessFeaturesToConstants.put("CutWhatever", "CutWhatever");
		this.preprocessFeaturesToConstants.put("CutNoDepartment", "CutNoDepartment");
		this.preprocessFeaturesToConstants.put("CutNoManager", "CutNoManager");
		this.preprocessFeaturesToConstants.put("Total", "Total");
		this.preprocessFeaturesToConstants.put("TotalWalker", "TotalWalker");
		this.preprocessFeaturesToConstants.put("TotalReducer", "TotalReducer");
		this.preprocessFeaturesToConstants.put("Precedence", "Precedence");
		this.preprocessFeaturesToConstants.put("GUI", "GUI");

		this.preprocessConstantsToFeatures = new HashMap<String, String>();
		
		for(String feature : this.preprocessFeaturesToConstants.keySet()){
			this.preprocessConstantsToFeatures.put(this.preprocessFeaturesToConstants.get(feature), feature);
		}
	}

	@Override
	public void generateProduct(Product product, String pathSPL, ResultadoLPS resultado) throws AssetNotFoundException, IOException, DirectoryException {
		
		if(!product.isGenerated()){
			ArrayList<String> assetsOrigens = new ArrayList<String>();
			ArrayList<String> assetsDestinos = new ArrayList<String>();
			
			product.sortAssetNames(assetsOrigens, assetsDestinos);
			
			this.createDirs(product, assetsDestinos, pathSPL);
			
			this.filesManager.copyFilesDirectory(pathSPL, assetsOrigens, assetsDestinos, product.getPath(), true);
			
			
			File productDirectory = new File(product.getPath());
			HashSet<String> featuresList = product.getFeaturesList();
			this.preprocessVelocity(featuresList, productDirectory, product.getSpl(), product.getPath(), resultado);
			
			String libPath = product.getSpl().getLibPath();
			
			if(libPath != null){
				FilesManager.getInstance().copyLibs(libPath, product.getPath() + File.separator + "lib");
			}
			
			product.setGenerated(true);
			resultado.getMeasures().setQuantidadeProdutosCompilados(resultado.getMeasures().getQuantidadeProdutosCompilados() + 1);
		}
	}
}
	