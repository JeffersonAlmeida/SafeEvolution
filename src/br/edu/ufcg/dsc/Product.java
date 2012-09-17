package br.edu.ufcg.dsc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Product {
	
	private ProductLine spl;

	private int id;
	
	private HashSet<String> featuresList;
	
	private HashMap<String,String> mapeamentoAssetNameParaOrigem;
	private HashMap<String,String> mapeamentoAssetNameParaDestino;
	
	private Product likelyCorrespondingProduct;
	
	private boolean generated;
	private boolean compiled;

	private String path;

	private HashSet<String> preProcessTags;

	public Product(ProductLine spl, int id, HashSet<String> features, HashSet<String> preProcessTags, 
			HashMap<String,String> mapeamentoAssetNameParaOrigem, HashMap<String,String> mapeamentoAssetNameParaDestino) {
		this.spl = spl;
		
		this.id = id;
		
		this.featuresList = features;
		this.preProcessTags = preProcessTags;
		
		this.mapeamentoAssetNameParaOrigem = mapeamentoAssetNameParaOrigem;
		this.mapeamentoAssetNameParaDestino = mapeamentoAssetNameParaDestino;
		
		this.generated = false;
		this.compiled = false;
	}
	
	public ProductLine getSpl() {
		return spl;
	}

	public int getId() {
		return id;
	}

	public HashSet<String> getFeaturesList() {
		return featuresList;
	}

	public String getDefaultDir() {
		return Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product"
				+ id;
	}
	
	public int totalOfFeatures(){
		return featuresList.size();
	}
	
	public String toString(){
		return "Product " + id + " with features " + featuresList.toString();
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setFeaturesList(HashSet<String> featuresList) {
		this.featuresList = featuresList;
	}

	public Product getLikelyCorrespondingProduct() {
		return likelyCorrespondingProduct;
	}

	public void setLikelyCorrespondingProduct(Product likelyCorrespondingProduct) {
		this.likelyCorrespondingProduct = likelyCorrespondingProduct;
	}

	public HashMap<String, String> getMapeamentoAssetNameParaOrigem() {
		return mapeamentoAssetNameParaOrigem;
	}

	public void setMapeamentoAssetNameParaOrigem(
			HashMap<String, String> mapeamentoAssetNameParaOrigem) {
		this.mapeamentoAssetNameParaOrigem = mapeamentoAssetNameParaOrigem;
	}

	public HashMap<String, String> getMapeamentoAssetNameParaDestino() {
		return mapeamentoAssetNameParaDestino;
	}

	public void setMapeamentoAssetNameParaDestino(
			HashMap<String, String> mapeamentoAssetNameParaDestino) {
		this.mapeamentoAssetNameParaDestino = mapeamentoAssetNameParaDestino;
	}

	/**
	 * Compara se mapeamento entre nomes e assets eh o mesmo nos dois 
	 * produtos.
	 * @param productSource
	 * @return
	 */
	public boolean temMesmosAssetsEPreProcessConstants(Product productSource) {
		return this.mapeamentoAssetNameParaDestino.equals(productSource.mapeamentoAssetNameParaDestino) &&
			this.mapeamentoAssetNameParaOrigem.equals(productSource.mapeamentoAssetNameParaOrigem) &&
			this.preProcessTags.equals(productSource.preProcessTags);
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath(){
		return this.path;
	}

	public boolean isCompiled() {
		return compiled;
	}

	public void setCompiled(boolean compiled) {
		this.compiled = compiled;
	}

	public boolean containsSomeFeature(HashSet<String> changedFeatures) {
		boolean result = false;
		
		for(String feature : changedFeatures){
			if(this.featuresList.contains(feature)){
				result = true;
				
				break;
			}
		}
		
		return result;
	}

	public void sortAssetNames(ArrayList<String> assetsOrigens, ArrayList<String> assetsDestinos) {
		for(String assetName : this.mapeamentoAssetNameParaOrigem.keySet()){
			assetsOrigens.add(this.mapeamentoAssetNameParaOrigem.get(assetName));
			assetsDestinos.add(this.mapeamentoAssetNameParaDestino.get(assetName));
		}
	}

	public HashSet<String> getPreProcessTags() {
		return preProcessTags;
	}

	public void setPreProcessTags(HashSet<String> preProcessTags) {
		this.preProcessTags = preProcessTags;
	}

	public boolean containsSomeAsset(Collection<String> classesModificaadas, HashMap<String, String> mapping) {
		boolean result = false;
		
		for(String classe : classesModificaadas){
			String path = mapping.get(classe);
			
			for(String assetPath : this.mapeamentoAssetNameParaOrigem.values()){
				if(result){
					break;
				}
				
				if(this.getComparablePath(path).contains(this.getComparablePath(assetPath))){
					result = true;
					
					break;
				}
			}
		}
		
		return result;
	}
	
	private String getComparablePath(String path) {
		String result = "";

		path = path.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/");
		
		String[] parts = path.split("src");

		for(int i = 1; i < parts.length; i++){
			result = result + "src" + parts[i];
		}

		return result;
	}
}
