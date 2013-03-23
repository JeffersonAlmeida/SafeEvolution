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
	
		/**
		 * @return {@link Boolean} T
		 * @throws AssetNotFoundException 
		 * @throws IOException 
		 * @throws DirectoryException 
		 */
		public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, AssetNotFoundException, DirectoryException{
			boolean isRefactoring = true;
			boolean isSPLWellFormed = this.wellFormedness.isWF(sourceLine, targetLine);
			if(isSPLWellFormed){
					for (Product productSource : sourceLine.getProducts()) {
						productSource.printSetOfFeatures();
						this.productBuilder.generateProduct(productSource, sourceLine.getPath());
						Product provavelCorrespondente = productSource.getLikelyCorrespondingProduct();
		
							if (provavelCorrespondente != null) {
								this.productBuilder.generateProduct(provavelCorrespondente, targetLine.getPath());
								isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, provavelCorrespondente, sourceLine.getControladoresFachadas(), propertiesObject);
							} else {
								/* If the source product does not have a correspondent target product it is NOT considered a refactoring.
								 * It means, that the behavior was not preserved once we can not find even a correspondent target product.*/
								System.out.println("This product does not have a correspondent target product.");
								isRefactoring = false;
							}
		
							//Testa se o comportamento nao bate com nenhum outro destino. Exceto para o caso de NAIVE_WITHOUT_RENAMING.
							if (!isRefactoring) {
								for (Product productTarget : targetLine.getProducts()) {
									if (productTarget != provavelCorrespondente) {
										this.productBuilder.generateProduct(productTarget, targetLine.getPath());
		
										isRefactoring = CommandLine.isRefactoring(productSource, productTarget, sourceLine.getControladoresFachadas(), propertiesObject);
		
										//Para de procurar se encontrar um par com mesmo comportamento.
										if (isRefactoring) {
											break;
										}
									}
								}
							}
		
							/* If one method is not a refactoring, we can break this loop and let the user know about the refactoring was not applied successfully. */
							/* Source and Target does not have compatible observable behavior. */
							if (!isRefactoring) {
								break;
							}
				   }
		    }
		    return isRefactoring;
		}
	
}
