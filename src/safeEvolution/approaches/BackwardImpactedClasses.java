package safeEvolution.approaches;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import safeEvolution.approaches.optimizations.ImpactedClasses;
import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class BackwardImpactedClasses  extends ImpactedClasses{
	
	public BackwardImpactedClasses(ProductBuilder productBuilder, FilePropertiesObject in, Collection<String> extendedImpactedClasses) {
		super(productBuilder, in, extendedImpactedClasses);
		this.printListofExtendedImpactedClasses(extendedImpactedClasses.iterator());
	}

	public boolean evaluate(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, boolean wf, boolean areAllProductsMatched) throws AssetNotFoundException, IOException, DirectoryException{
		boolean isRefinement = false;
		if(wf && areAllProductsMatched){
			if(super.getModifiedClasses().isEmpty()){
				System.out.println("\nThere is not Extended Impacted classes to verify.");
				return true;
			}else{
				return checkAssetMappingBehavior(sourceSPL, targetSPL, changedFeatures);
			}
		}else{  // Create an Exception!!
			System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
		}
		return isRefinement;
	}

	private void printListofExtendedImpactedClasses(Iterator<String> i) {
		System.out.println("\nList of Extended Impacted Classes: ");
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println("\n--------------------------");
	}
}
