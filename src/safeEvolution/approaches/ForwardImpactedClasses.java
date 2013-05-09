package safeEvolution.approaches;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import safeEvolution.approaches.optimizations.ImpactedClasses;
import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class ForwardImpactedClasses extends ImpactedClasses {
	
		public ForwardImpactedClasses(ProductBuilder productBuilder, FilePropertiesObject in, Collection<String> modifiedClasses) {
			super(productBuilder, in, modifiedClasses);
		}
		
		public boolean evaluate(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, boolean wf, boolean areAllProductsMatched) throws AssetNotFoundException, IOException, DirectoryException{
			boolean isRefinement = false;
			if(wf && areAllProductsMatched){
				return checkAssetMappingBehavior(sourceSPL, targetSPL, changedFeatures);	
			}else{  // Create an Exception!!
				System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
			}
			return isRefinement;
			
		}
}
