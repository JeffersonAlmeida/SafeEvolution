package br.cin.ufpe.br.approaches;

import java.io.IOException;
import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
import br.cin.ufpe.br.wf.WellFormedness;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class AllProductPairs {
	
		private WellFormedness wellFormedness;
		private ProductBuilder productBuilder;
		
		public AllProductPairs(WellFormedness wellFormedness, ProductBuilder pBuilder) {
			super();
			this.wellFormedness = wellFormedness;
			this.productBuilder = pBuilder;
		}
	
		public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, AssetNotFoundException, DirectoryException{
			boolean isRefactoring = true;
			boolean isSPLWellFormed = this.wellFormedness.isWF(sourceLine, targetLine);
			if(isSPLWellFormed){
				for (Product productSource : sourceLine.getProducts()) {
					productSource.printSetOfFeatures();
					this.productBuilder.generateProduct(productSource, sourceLine.getPath());
					Product probablyCorrespondentProduct = productSource.getLikelyCorrespondingProduct();
					/* if these two products do not have the same behavior, APP approach try to find out another product that is behaviorally corresponding to this one in the set of target products */
					if(!(isRefactoring = haveSameBehavior(sourceLine, targetLine, propertiesObject, isRefactoring, productSource, probablyCorrespondentProduct))){
						if(!(isRefactoring = tryToFindCorrespondentProduct(sourceLine, targetLine, propertiesObject,productSource, probablyCorrespondentProduct )))
							break;  /* APP approach reports a Non - Refinement because It was not able to find a correspondent product even though  with brute force algorithm */
					}
			    }
		    }
		    return isRefactoring;
		}
		
		/**
		 * Try to find out another product that is behaviorally corresponding to this one in the set of target products
		 */
		private boolean tryToFindCorrespondentProduct(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, Product productSource, Product probablyCorrespondentProduct)throws AssetNotFoundException, IOException, DirectoryException {
			boolean isRefactoring = false;
			for (Product productTarget : targetLine.getProducts()) {
				this.productBuilder.generateProduct(productTarget, targetLine.getPath());
				if(productTarget!=probablyCorrespondentProduct){
					if (isRefactoring = CommandLine.isRefactoring(productSource, productTarget, sourceLine.getControladoresFachadas(), propertiesObject)) {
						break;
					}
				}
			}
			return isRefactoring;
		}
		
		private boolean haveSameBehavior(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, boolean isRefactoring, Product productSource, Product probablyCorrespondentProduct) throws AssetNotFoundException, IOException, DirectoryException {
			if (probablyCorrespondentProduct != null) {
				this.productBuilder.generateProduct(probablyCorrespondentProduct, targetLine.getPath());
				isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, probablyCorrespondentProduct, sourceLine.getControladoresFachadas(), propertiesObject);
			}
			return isRefactoring;
		}
}
