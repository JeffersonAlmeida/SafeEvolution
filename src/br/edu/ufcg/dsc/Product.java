package br.edu.ufcg.dsc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 * This Class represents a single product of the SPL.
 */
public class Product {
	
	private ProductLine spl;

	private int id;
	
	private HashSet<String> featuresList;
	
	private HashMap<String,String> mapeamentoAssetNameParaOrigem;
	private HashMap<String,String> mapeamentoAssetNameParaDestino;
	
	 /* It will store the corresponding product. Corresponding products has the same features.
       Not necessarily  the same assets and not necessarily  the same behavior. */
	private Product likelyCorrespondingProduct;
	
	private boolean generated;
	private boolean compiled;

	private String path;

	/* ? */
	private HashSet<String> preProcessTags;

	/**
	 * Product Constructor.
	 * @param spl
	 * @param id
	 * @param features
	 * @param preProcessTags
	 * @param mapeamentoAssetNameParaOrigem
	 * @param mapeamentoAssetNameParaDestino
	 */
	public Product(ProductLine spl, int id, HashSet<String> features, HashSet<String> preProcessTags, HashMap<String,String> mapeamentoAssetNameParaOrigem, HashMap<String,String> mapeamentoAssetNameParaDestino) {
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
	 * This Compares whether mapping between names and assets is the same in both products.
	 * @param productSource
	 * @return
	 */
	public boolean temMesmosAssetsEPreProcessConstants(Product productSource) {
		
		Iterator<String> it = mapeamentoAssetNameParaDestino.keySet().iterator();
		Iterator<String> it2 = productSource.mapeamentoAssetNameParaDestino.keySet().iterator();
		int i = 0 ;
		System.out.println("\n\t Mapping Asset Destiny");
		while(it.hasNext()){
			String key = (String) it.next();
			System.out.println(" key " + (i++) + ": "+ key + " content: " + mapeamentoAssetNameParaDestino.get(key));
		}
		i=0;
		while(it2.hasNext()){
			String key2 = (String) it2.next();
			System.out.println(" key " + (i++) + ": "+ key2 + " content: " + mapeamentoAssetNameParaDestino.get(key2));
		}
		
		boolean mappingAssetDestiny = this.compareAssets(productSource);
		
		
		//boolean mappingAssetDestiny = this.mapeamentoAssetNameParaDestino.equals(productSource.mapeamentoAssetNameParaDestino);
		boolean mappingAssetOrigin = this.mapeamentoAssetNameParaOrigem.equals(productSource.mapeamentoAssetNameParaOrigem);
		boolean preProcess = this.preProcessTags.equals(productSource.preProcessTags);
		System.out.println("mappingAssetDestiny: " + mappingAssetDestiny + " mappingAssetOrigin: " + mappingAssetOrigin + " preProcess: " + preProcess);
		return mappingAssetDestiny && mappingAssetOrigin &&	preProcess;
	}

	private boolean compareAssets(Product productSource) {
		Iterator<String> it = mapeamentoAssetNameParaDestino.keySet().iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			//System.out.println("compara: Primeiro Asset: " + this.mapeamentoAssetNameParaDestino.get(key) + "  -  Segundo Asset: " + 	productSource.mapeamentoAssetNameParaDestino.get(key) );
			if(!(this.mapeamentoAssetNameParaDestino.containsKey(key))){
				System.out.println("Chave não encontrada: " + key);
				return false;
			}
		}
		return true;
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

	/**
	 * 
	 * @param assetsOrigens
	 * @param assetsDestinos
	 */
	public void sortAssetNames(ArrayList<String> assetsOrigens, ArrayList<String> assetsDestinos) {
		for(String assetName : this.mapeamentoAssetNameParaOrigem.keySet()){
			assetsOrigens.add(this.mapeamentoAssetNameParaOrigem.get(assetName));
			assetsDestinos.add(this.mapeamentoAssetNameParaDestino.get(assetName));
		}
		
		System.out.println("\nAssests Origem");
		Iterator<String> i = assetsOrigens.iterator();
		while(i.hasNext()){
			String asset = (String) i.next();
			System.out.println(" asset: " + asset );
		}
		
		System.out.println("\nAssests Destino");
		Iterator<String> i2 = assetsDestinos.iterator();
		while(i2.hasNext()){
			String asset = (String) i2.next();
			System.out.println(" asset: " + asset );
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

	public void printSetOfFeatures() {
		String concat =  "";
		Iterator<String> i = this.featuresList.iterator();
		while(i.hasNext()){
			String feature = (String) i.next();
			concat = concat + " [ " + feature + " ]";
		}
		System.out.println("\nProduct" + this.id +  " :: " + concat);
	}
}
