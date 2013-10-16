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

	public boolean evaluate(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, boolean wf, boolean areAllProductsMatched,  Collection<String> ic) throws AssetNotFoundException, IOException, DirectoryException{
		boolean isRefinement = false;
		if(wf && areAllProductsMatched){
			return run(sourceSPL, targetSPL, changedFeatures, ic);
		}else{  // Create an Exception!!
			System.out.println("\nERROR: It is not possible to apply this tool, because Well-Formedness: " + wf + " product Matching: " + areAllProductsMatched);
		}
		return isRefinement;
	}

	private boolean run(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, Collection<String> ic) throws AssetNotFoundException, DirectoryException, IOException {
		if(!super.getModifiedClasses().isEmpty())
			return checkAssetMappingBehavior(sourceSPL, targetSPL, changedFeatures);
		else{
			if (!ic.isEmpty()){
				super.setModifiedClasses(ic);
				return checkAssetMappingBehavior(sourceSPL, targetSPL, changedFeatures);
			}else
				System.out.println("\nThere is neither IC nor EIC to analyze.");
			return true;
		}
	}

	private void printListofExtendedImpactedClasses(Iterator<String> i) {
		System.out.println("\nList of Extended Impacted Classes: ");
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println("\n--------------------------");
	}
	
}
