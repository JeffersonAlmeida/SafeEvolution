package safeEvolution.productMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;

public class ProductMatching {
	
	private ProductBuilder productBuilder;
	
	public ProductMatching(ProductBuilder productBuilder) {
		super();
		this.productBuilder = productBuilder;
	}

	/**
	 * Verify whether Configuration knowledge and Feature Model is a refinement.<br></br>
	 * @param sourceLine
	 * @param targetLine
	 * @return
	 * @throws IOException
	 * @throws AssetNotFoundException
	 */
	public boolean areAllProductsMatched(ProductLine sourceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
	
		boolean isRefinement = true;

		/*IT will store the set of SOURCE PRODUCT LINE features*/
		HashSet<HashSet<String>> setsOfFeaturesSource = sourceLine.getSetsOfFeatures();
		System.out.println("\n# SOURCE Products List<"+ setsOfFeaturesSource.size() +"> #");
		sourceLine.printSetOfFeatures();
		
		/*IT will store the set of TARGET PRODUCT LINE features*/
		HashSet<HashSet<String>> setsOfFeaturesTarget = targetLine.getSetsOfFeatures();
		System.out.println("\n# TARGET Products List<"+ setsOfFeaturesTarget.size()+"> #");
		targetLine.printSetOfFeatures();

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
				System.out.println("\nProduct  :: " + concat);
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

		System.out.println("\n\nAll products in the source have a really correspondent target product ?: " + isRefinement);
		return isRefinement;
	} /*Method end*/
	
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
		System.out.println("\nProduct "+ (id++) + " :: " + concat);
	
		/* <AssetName, path> Get in CK the DESTINY of the assets. */
 		HashMap<String, String> constantesDestinos = productLine.getCk().evalCKDestinos(featureSet);
		
		System.out.println("constante destino esta vazio: " + constantesDestinos.isEmpty());

		/* <AssetName, path> Get in CK the ORIGIN of the assets.*/
		HashMap<String, String> constantesOrigens = new HashMap<String, String>();

		for (String constant : constantesDestinos.keySet()) {
			/* Replacing invalid destinations, where source and destination are the same. */
			constantesDestinos.put(constant, constantesDestinos.get(constant) == null ? productLine.getAssetMapping().get(constant.trim()) : constantesDestinos.get(constant));

			/* The origin path is always the one informed in the asset mapping. */
			constantesOrigens.put(constant, productLine.getAssetMapping().get(constant.trim()));
		}
		
		HashSet<String> preProcessTags = this.productBuilder.getPreProcessTags(featureSet);
		
		/*Constructs a new Product */
		return new Product(productLine, id, featureSet, preProcessTags, constantesOrigens, constantesDestinos);
	}
	
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
}
