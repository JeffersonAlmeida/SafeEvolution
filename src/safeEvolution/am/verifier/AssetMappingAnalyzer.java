package safeEvolution.am.verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.naming.ConfigurationException;
import org.eclipse.jdt.core.JavaModelException;
/*import org.eclipse.jdt.core.JavaModelException;*/
import br.edu.ufcg.dsc.ast.ASTComparator;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.util.FileManager;

public class AssetMappingAnalyzer {
	
	/** A string collection of changed classes.*/
	private Collection<String> modifiedClassesList;
	/** This variable will store the changed assets - mofified files, classes, etc .*/
	private HashSet<String> changedAssetsList;
	private ASTComparator astComparator;
	private boolean alreadyVerified;
	private boolean isSameAssets;
	
	public AssetMappingAnalyzer() {
		System.out.println("Asset Mapping Analyzer");
		this.astComparator = new ASTComparator();
		try {
			this.astComparator.setUpProject();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/** It looks for the modified assets. 
	 * @throws JavaModelException */
	public boolean isSameAssets(ProductLine sourceLine, ProductLine targetLine) throws JavaModelException {
		if(this.alreadyVerified)
			return this.isSameAssets;
		
		this.isSameAssets = true;
		
		/* Get all SOURCE product line classes. */
		Set<String> sourceKeySet = sourceLine.getMappingClassesSistemaDeArquivos().keySet();
		
		/* Get all TARGET product line classes. */
		Set<String> targetKeySet = targetLine.getMappingClassesSistemaDeArquivos().keySet();

		/* Initialize the modified classes variable. */
		this.modifiedClassesList = new HashSet<String>();
		
		/* Initialize the changed assets variable. */
		this.changedAssetsList = new HashSet<String>();

		/* walk through all assets of the SOURCE product line classes. */
		for (String asset : sourceKeySet) {
			/* location source file*/
			String locationSource = sourceLine.getMappingClassesSistemaDeArquivos().get(asset);
			/* location target file*/
			String locationTarget = targetLine.getMappingClassesSistemaDeArquivos().get(asset);
			if (locationSource != null && locationTarget != null) {
				/* build the two files.*/
				File sourceFile = new File(locationSource); // source asset File.
				File targetFile = new File(locationTarget); // target asset File.
				try {
					boolean equals;
					if (asset.endsWith("java")) {
						/* This methods compares two textual files and return whether they are equal.
						 * It means, there is no refactoring in the second when compared to the first one. */
						this.astComparator.setInputs(sourceFile, targetFile);
						equals = this.astComparator.isIsomorphic();
					} else if (asset.endsWith("aj")) {
						/* This methods compares two textual files and return whether they are equal. 
						 * It means, there is no refactoring in the second when compared to the first one. */
						equals = this.isTextualFileContentsEquals(sourceFile, targetFile);
					} else {
						equals = true;
					}
					if (!equals) {
						this.isSameAssets = false;
						/*Put the asset in the modified classes.*/
						this.modifiedClassesList.add(asset);
						this.changedAssetsList.add(FileManager.getInstance().getPath("src." + asset));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!targetKeySet.containsAll((sourceKeySet))) {
			this.isSameAssets = false;
		}
		System.out.println("\nHave Source and Target the same assets ?:" + this.isSameAssets +" \n");
		this.alreadyVerified = true;
		return this.isSameAssets;
	}
	
	/**
	 * This methods compares two textual files and return whether they are equal. 
	 * It means, there is no refactoring in the second when compared to the first one.
	 */
	private boolean isTextualFileContentsEquals(File sourceFile, File targetFile) {
		boolean result = true;
		String linhaSource = "";
		String linhaTarget = "";
		try {
			FileReader readerSource = new FileReader(sourceFile);
			FileReader readerTarget = new FileReader(targetFile);
			BufferedReader inSource = new BufferedReader(readerSource);
			BufferedReader inTarget = new BufferedReader(readerTarget);
			while (result && ((linhaSource = inSource.readLine()) != null & (linhaTarget = inTarget.readLine()) != null)) {
				if (!linhaSource.trim().equals(linhaTarget.trim())) {
					result = false;
				}
			}
			if (linhaSource != null || linhaTarget != null) {
				result = false;
			}
			inSource.close();
			inTarget.close();
			readerSource.close();
			readerTarget.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/*************************************************************Getters and Setters */
	public Collection<String> getModifiedClassesList() {
		return modifiedClassesList;
	}
	public void setModifiedClassesList(Collection<String> modifiedClassesList) {
		this.modifiedClassesList = modifiedClassesList;
	}
	public HashSet<String> getChangedAssetsList() {
		return changedAssetsList;
	}
	public void setChangedAssetsList(HashSet<String> changedAssetsList) {
		this.changedAssetsList = changedAssetsList;
	}
	
}
