package safeEvolution.am.verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.naming.ConfigurationException;
import org.eclipse.jdt.core.JavaModelException;

import soot.Main;
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
	private boolean canIncrementVerificationCounter;
	private int sourceCodeVerificationCounter;
	/** A string collection of changed classes.*/
	protected Collection<String> impactedClasses;
	private Collection<String> extendedImpactedClasses;
	private String excludes;
	
	public AssetMappingAnalyzer() {
		System.out.println("Asset Mapping Analyzer");
		this.excludes = "Action";
		this.canIncrementVerificationCounter = true;
		this.sourceCodeVerificationCounter = 1;
		this.extendedImpactedClasses = new HashSet<String>();
		this.impactedClasses = new HashSet<String>();
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
	
	public void findExtendedImpactedClasses(File sourceSplDirectory){
		if(!this.getModifiedClassesList().isEmpty()){
			this.impactedClasses.addAll(this.modifiedClassesList);
			this.getBackwardDependencies(sourceSplDirectory);	
		}
	}
	
	// Codigo para encontrar as dependencias em mais de um nivel acima
	private void getBackwardDependencies(File sourceSplDirectory){
		int i = 0;
		while(i < this.sourceCodeVerificationCounter){
			this.canIncrementVerificationCounter = true;
			this.getAboveDependencies(sourceSplDirectory);
			i++;
		}
	}
	
	private void getDependencies(File classe) {
		if(!(thisclassBelongsToModifiedClasses(classe))){
			Collection<String> dependencias = Main.v().getDependences(classe.getName().replaceAll(".java", ""), classe.getParent());  // Get All Dependencies of this Class
			if(!(dependencias.isEmpty())){
				clazzDependenciesBelongToModifiedClasses(getPackageName(classe), dependencias); // A -> B  A is dependent of B.   B is a dependency of A	
			}
		}
	}
	
	private void clazzDependenciesBelongToModifiedClasses(String classe, Collection<String> dependencias) {
		Iterator<String> i = dependencias.iterator();
		while(i.hasNext()){
			String s = i.next();
			s = s + ".java";
			if(s.contains("br.ufpe")){
					System.out.println("\nDependencia: " + s);
					Iterator<String> iterator2 = this.impactedClasses.iterator();
					while(iterator2.hasNext()){
						String string2 = iterator2.next();
						/*String[] words = string2.split("\\.");//words[words.length-1];^M
						String w = words[words.length-2];*/
		                if(string2.contains(s)){
		                	   addToEIC(classe);
		                       break;
		                }
	                }
            }
		}
	}

	private void addToEIC(String classe) {
		//this.impactedClasses.add(classe);
		if(!classe.contains(this.excludes)){
			   this.extendedImpactedClasses.add(classe); // Add class in the dependencies of modified classes set.^M
			   //if (canIncrementVerificationCounter){ this.sourceCodeVerificationCounter++; this.canIncrementVerificationCounter = false
		}
	}

	
	private boolean thisclassBelongsToModifiedClasses(File classe) {
		return this.impactedClasses.contains(this.getPackageName(classe));
	}
	
	private String getPackageName(File classe) {
		String words[] = classe.getAbsolutePath().split("src/");
		String packagePath =  "";
		if(words.length>2){
			/* Target Case */
			packagePath = words[words.length-2] + "src/" +words[words.length-1];
		}else if (words.length==2){
			packagePath =  words[1];
		}
		return packagePath.replaceAll("/", ".");
	}
	
	private void getAboveDependencies(File classe) {
		System.out.println("\nFILE: " + classe.getAbsolutePath());
		if (classe.isDirectory() && !classe.getAbsolutePath().contains(".svn")){ 
			File[] files = classe.listFiles();
			for (File subFile : files) {
				this.getAboveDependencies(subFile);
			}
		} else if (classe.getAbsolutePath().endsWith("java") && !classe.getAbsolutePath().contains("ProjectManagerController")) 
			getDependencies(classe);
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
	public Collection<String> getExtendedImpactedClasses() {
		return extendedImpactedClasses;
	}
	public void setExtendedImpactedClasses(Collection<String> extendedImpactedClasses) {
		this.extendedImpactedClasses = extendedImpactedClasses;
	}
	public Collection<String> getImpactedClasses() {
		return impactedClasses;
	}
	public void setImpactedClasses(Collection<String> impactedClasses) {
		this.impactedClasses = impactedClasses;
	}
}
