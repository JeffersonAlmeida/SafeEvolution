package safeEvolution.approaches;

import java.io.IOException;
import java.util.Collection;

import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class ImpactedProducts {
		
		/** A string collection of changed classes.*/
		private Collection<String> modifiedClasses;
		private ProductBuilder productBuilder;
		
		public ImpactedProducts(ProductBuilder productBuilder,Collection<String> modifiedClasses ) {
			super();
			this.productBuilder = productBuilder;
			this.modifiedClasses = modifiedClasses;
		}

		public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, boolean wf, boolean areAllProductsMatched) throws AssetNotFoundException, IOException, DirectoryException{
			boolean isRefactoring = true;
			if(wf && areAllProductsMatched){
				for (Product productSource : sourceLine.getProducts()) {
					this.productBuilder.generateProduct(productSource, sourceLine.getPath());
					if(productSource.containsSomeAsset(this.modifiedClasses, sourceLine.getMappingClassesSistemaDeArquivos())){
						if(!(isRefactoring = haveSameBehavior(sourceLine, targetLine, propertiesObject, isRefactoring, productSource, productSource.getLikelyCorrespondingProduct())))
							break;
					}
				}
			}else { 				// Create an Exception!!
				System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
			}
			return isRefactoring;
		}
		
		private boolean haveSameBehavior(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject, boolean isRefactoring, Product productSource, Product probablyCorrespondentProduct) throws AssetNotFoundException, IOException, DirectoryException {
			if (probablyCorrespondentProduct != null){
				this.productBuilder.generateProduct(probablyCorrespondentProduct, targetLine.getPath());
				isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, probablyCorrespondentProduct, sourceLine.getControladoresFachadas(), propertiesObject);
			}
			return isRefactoring;
		}

		/*......................................................... Getters and Setters */
		public void setProductBuilder(ProductBuilder productBuilder) {
			this.productBuilder = productBuilder;
		}
		public ProductBuilder getProductBuilder() {
			return productBuilder;
		}
}
