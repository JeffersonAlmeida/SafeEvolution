package safeEvolution.approaches;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import soot.Main;
import safeEvolution.approaches.optimizations.ImpactedClasses;
import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class BackwardImpactedClasses  extends ImpactedClasses{
	
	private Collection<String> extendedImpactedClasses;
	private int sourceCodeVerificationCounter;
	private boolean canIncrementVerificationCounter;
	public BackwardImpactedClasses(ProductBuilder productBuilder, FilePropertiesObject in, Collection<String> modifiedClassesList) {
		super(productBuilder, in, modifiedClassesList);
		this.extendedImpactedClasses = new HashSet<String>();
		this.sourceCodeVerificationCounter = 1;
		this.canIncrementVerificationCounter = true;
	}

	public boolean evaluate(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, boolean wf, boolean areAllProductsMatched) throws AssetNotFoundException, IOException, DirectoryException{
		/*this.getBackwardDependencies(new File(sourceSPL.getPath()+"src")); Codigo para encontrar as dependencias em mais de um nivel acima*/ 
		
		//this.getAboveDependencies(new File(sourceSPL.getPath()+"src"));   
		
		/* Test on Graphical User Interface with Validation class associated with */
		this.extendedImpactedClasses.add("lancs.mobilemedia.core.ui.controller.BaseController.java");
		this.printListofExtendedImpactedClasses();
		super.setModifiedClasses(this.extendedImpactedClasses); // Impacted Classes is Extended Impacted Classes now
		return super.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched);
	}
	
	private void printListofExtendedImpactedClasses() {
		Iterator<String> i = this.extendedImpactedClasses.iterator();
		System.out.println("\nList of Extended Impacted Classes: " + this.extendedImpactedClasses.size());
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println("\n--------------------------");
	}

	// Codigo para encontrar as dependencias em mais de um nivel acima
	/*private void getBackwardDependencies(File sourceSplDirectory){
		int i = 0;
		while(i < this.sourceCodeVerificationCounter){
			this.canIncrementVerificationCounter = true;
			this.getAboveDependencies(sourceSplDirectory);
			i++;
		}
	}*/
	
	private void getAboveDependencies(File classe) {
		System.out.println("\nFILE: " + classe.getAbsolutePath());
		if (classe.isDirectory() && !classe.getAbsolutePath().contains(".svn") ) { 
			File[] files = classe.listFiles();
			for (File subFile : files) {
				this.getAboveDependencies(subFile);
			}
		} else if (classe.getAbsolutePath().endsWith("java")) 
			getDependencies(classe);
	}

	private void getDependencies(File classe) {
		if(!(thisclassBelongsToModifiedClasses(classe))){
			Collection<String> dependencias = Main.v().getDependences(classe.getName().replaceAll(".java", ""), classe.getParent());  // Get All Dependencies of this Class
			if(!(dependencias.isEmpty())){
				clazzDependenciesBelongToModifiedClasses(getPackageName(classe), dependencias); // A -> B  A is dependent of B.   B is a dependency of A	
			}
		}
	}
	
	private boolean thisclassBelongsToModifiedClasses(File classe) {
		return super.modifiedClasses.contains(this.getPackageName(classe));
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
	
	private void clazzDependenciesBelongToModifiedClasses(String classe, Collection<String> dependencias) {
		Iterator<String> i = dependencias.iterator();
		while(i.hasNext()){
			String s = i.next();
			System.out.println("\nDependencia: " + s);
			Iterator<String> iterator2 = this.modifiedClasses.iterator();
			while(iterator2.hasNext()){
				String string2 = iterator2.next();
				String[] words = string2.split("\\.");//words[words.length-1];
				String w = words[words.length-2];
				if(s.equals(w)){
					this.modifiedClasses.add(classe);
					this.extendedImpactedClasses.add(classe); // Add class in the dependencies of modified classes set.
					/*if (canIncrementVerificationCounter){ this.sourceCodeVerificationCounter++; this.canIncrementVerificationCounter = false;} Codigo para encontrar as dependencias em mais de um nivel acima*/
					break;
				}
			}
		}
	}
}
