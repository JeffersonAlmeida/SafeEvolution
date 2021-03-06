package safeEvolution.approaches;

import java.io.IOException;

import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
public class AllProducts {
	
	private ProductBuilder productBuilder;
	public AllProducts(ProductBuilder productBuilder) {
		super();
		this.productBuilder = productBuilder;
	}

	public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, boolean wf, boolean areAllProductsMatched) throws IOException, AssetNotFoundException, DirectoryException{
		boolean isRefactoring = true;
		if(wf && areAllProductsMatched){
			for (Product productSource : sourceLine.getProducts()) {
				productSource.printSetOfFeatures();
				this.productBuilder.generateProduct(productSource, sourceLine.getPath());
				Product probablyCorrespondentProduct = productSource.getLikelyCorrespondingProduct();
				/* if these two products do not have the same behavior, AP approach reports a Non - Refinement */
				if(!(isRefactoring = haveSameBehavior(sourceLine, targetLine, propertiesObject, isRefactoring, productSource, probablyCorrespondentProduct))){
					break;
				}
		    }
	    }else {  // Create an Exception!!
	    	System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
	    }
	    return isRefactoring;
	}

	private boolean haveSameBehavior(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, boolean isRefactoring, Product productSource, Product probablyCorrespondentProduct) throws AssetNotFoundException, IOException, DirectoryException {
		if (probablyCorrespondentProduct != null) {
			this.productBuilder.generateProduct(probablyCorrespondentProduct, targetLine.getPath());
			isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, probablyCorrespondentProduct, null, propertiesObject);
		}
		return isRefactoring;
	}
	
}
