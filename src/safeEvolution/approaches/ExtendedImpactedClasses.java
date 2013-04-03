package safeEvolution.approaches;

import java.io.IOException;

import safeEvolution.fileProperties.FilePropertiesObject;

import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class ExtendedImpactedClasses {
	
	
	
	public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, AssetNotFoundException, DirectoryException{
		return true;
	}
}
