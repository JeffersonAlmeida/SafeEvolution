package safeEvolution.alloy.products;

import java.util.HashMap;
import java.util.HashSet;

import safeEvolution.wellFormedness.WellFormedness;

import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.fm.AlloyFMEvolutionBuilder;

public class AlloyProductGenerator {
	
	private HashMap<String, HashSet<HashSet<String>>> productsCache;
	private WellFormedness wellFormedness;
	private ProductBuilder productBuilder;
	
	public AlloyProductGenerator(WellFormedness wellFormedness,ProductBuilder productBuilder) {
		super();
		this.productsCache = new HashMap<String, HashSet<HashSet<String>>>();
		this.wellFormedness = wellFormedness;
		this.productBuilder = productBuilder;
	}

	/** This method calls alloy to build source and target products and put it in cache.
	 * @param sourceLine Source Product Line.
	 * @param targetLine Target Product Line.
	 */
	public void generateProductsFromAlloyFile(ProductLine sourceLine, ProductLine targetLine) {
		System.out.println("\n\n\n\t\tLet's put the products in cache.\n");
		if (this.productsCache.get(sourceLine.getPath()) == null || this.productsCache.get(targetLine.getPath()) == null) {
			/*Build the Source Feature Model Alloy file.*/
			System.out.println("\nBuild the SOURCE Feature Model Alloy file:");
			this.wellFormedness.buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
			/*Build the Target Feature Model Alloy file.*/
			System.out.println("\nBuild the TARGET Feature Model Alloy file:");
			this.wellFormedness.buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);
			/*Build the Evolution Alloy file.*/
			System.out.println("\nBuild the EVOLUTION Alloy file:");
			buildFMEvolutionAlloyFile(sourceLine.getFmPath(), targetLine.getFmPath());
			if (this.productsCache.get(sourceLine.getPath()) == null) {
				/* This method calls alloy to build source products and put it in cache. */
				HashSet<HashSet<String>> productsSource = this.productBuilder.getProductsFromAlloy(Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME);
				this.productsCache.put(sourceLine.getPath(), productsSource);
			}
			if (this.productsCache.get(targetLine.getPath()) == null) {
				/* This method calls alloy to build target products and put it in cache. */
				HashSet<HashSet<String>> productsTarget = this.productBuilder.getProductsFromAlloy(Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME);
				this.productsCache.put(targetLine.getPath(), productsTarget);
			}
		}
		sourceLine.setSetsOfFeatures(this.productsCache.get(sourceLine.getPath()));
		targetLine.setSetsOfFeatures(this.productsCache.get(targetLine.getPath()));
		System.out.println("\n\t\tThe products are already in cache.");
	}
	
	private void buildFMEvolutionAlloyFile(String sourceFMXML, String targetFMXML) {
		AlloyFMEvolutionBuilder evolutionAlloy = new AlloyFMEvolutionBuilder();
		evolutionAlloy.buildAlloyFile("evolution", Constants.ALLOY_PATH + Constants.EVOLUTION_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, "source", sourceFMXML, "target", targetFMXML);
	}
	public HashMap<String, HashSet<HashSet<String>>> getProductsCache() {
		return productsCache;
	}
	public void setProductsCache(
			HashMap<String, HashSet<HashSet<String>>> productsCache) {
		this.productsCache = productsCache;
	}
}
