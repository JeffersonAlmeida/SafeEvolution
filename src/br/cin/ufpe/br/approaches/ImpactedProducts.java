package br.cin.ufpe.br.approaches;

import java.io.IOException;
import java.util.Collection;
import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
import br.cin.ufpe.br.wf.WellFormedness;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class ImpactedProducts {
		
		/** A string collection of changed classes.*/
		private Collection<String> modifiedClasses;
		
		private WellFormedness wellFormedness;
		private ProductBuilder productBuilder;
		
		public ImpactedProducts(WellFormedness wellFormedness, ProductBuilder productBuilder) {
			super();
			this.wellFormedness = wellFormedness;
			this.productBuilder = productBuilder;
		}

		public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws AssetNotFoundException, IOException, DirectoryException{
			boolean isRefactoring = true;
			boolean isSPLWellFormed = this.wellFormedness.isWF(sourceLine, targetLine);
			if(isSPLWellFormed){
				for (Product productSource : sourceLine.getProducts()) {
					productSource.containsSomeAsset(this.modifiedClasses, sourceLine.getMappingClassesSistemaDeArquivos());
					this.productBuilder.generateProduct(productSource, sourceLine.getPath());
					Product probablyCorrespondentProduct = productSource.getLikelyCorrespondingProduct();
					/* if these two products do not have the same behavior, IP approach reports a Non - Refinement */ 
					if(!(isRefactoring = haveSameBehavior(sourceLine, targetLine, propertiesObject, isRefactoring, productSource, probablyCorrespondentProduct))){
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
		

		/*......................................................... Getters and Setters */
		public void setProductBuilder(ProductBuilder productBuilder) {
			this.productBuilder = productBuilder;
		}
		public ProductBuilder getProductBuilder() {
			return productBuilder;
		}
		public void setWellFormedness(WellFormedness wellFormedness) {
			this.wellFormedness = wellFormedness;
		}
		public WellFormedness getWellFormedness() {
			return wellFormedness;
		}
}
