package safeEvolution.approaches;

import java.io.IOException;
import java.util.Collection;

import safeEvolution.fileProperties.FilePropertiesObject;

import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class ExtendedImpactedClasses {
	
	private ProductBuilder productBuilder;
	
	/** A string collection of changed classes.*/
	private Collection<String> modifiedClasses;
	
	private FilePropertiesObject input;
	
	public ExtendedImpactedClasses(ProductBuilder productBuilder, FilePropertiesObject in, Collection<String> modifiedClassesList) {
		this.productBuilder = productBuilder;
		this.input = in;
		this.modifiedClasses = modifiedClassesList;
	}

	public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, AssetNotFoundException, DirectoryException{
		
		return true;
	}
}
