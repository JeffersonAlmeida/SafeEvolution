package safeEvolution.wellFormedness;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import edu.mit.csail.sdg.alloy4.Err;

import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.alloy.SafeCompositionVerifier;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.dsc.fm.FeatureModelReader;
import br.edu.ufcg.dsc.refactoringresults.SafeCompositionResult;
import br.edu.ufcg.dsc.util.FileManager;

public class WellFormedness {
	
		private String featureModelSourceSemantics;
		private String featureModelTargetSemantics;
		
		public WellFormedness(){
			super();
		}
		
		/**
		 * This Method builds FM Alloy FIle <br></br>
		 * @param moduleName
		 * @param sourceFmAlloyName
		 * @param productLine
		 */
		public void buildFMAlloyFile(String moduleName, String sourceFmAlloyName, ProductLine productLine) {
			
			FeatureModelReader featureModelReader = new FeatureModelReader();
	
			featureModelReader.readFM(moduleName, productLine.getFmPath());
	
			productLine.setFeatures(featureModelReader.getFeatures());
	
			System.out.println(moduleName+" Set OF Features: ");
			productLine.printFeatures(moduleName);
			
			if (moduleName.equals("source")) {
				this.featureModelSourceSemantics = featureModelReader.getSemanticsFM();
				System.out.println("\nSOURCE FM Semantics: " + this.featureModelSourceSemantics);
			} else if (moduleName.equals("target")) {
				this.featureModelTargetSemantics = featureModelReader.getSemanticsFM();
				System.out.println("\nTARGET FM Semantics: " + this.featureModelTargetSemantics);
			}
			
			try {
				featureModelReader.buildAlloyFile(moduleName, sourceFmAlloyName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		public void buildAlloyCKFile(String name, String fMSemantics, String indicator, ProductLine productLine) {
			ConfigurationKnowledge ck = productLine.getCk();
			ck.print(indicator);
			productLine.printPreprocessProperties(indicator);
			String alloy = ck.toAlloy();
	
			System.out.println("Alloy File Content: " + alloy);
			
			HashSet<String> correctSet = new HashSet<String>();
			System.out.println("\nCorrect Set of Features:");
			for (String string : productLine.getFeatures()) {
				System.out.println("\nFeature: " + string);
				correctSet.add(string.trim());
			}
			HashSet<String> ckSigs = ck.getSignatures();
			System.out.println("\nIncluding CK Signatures in Correct Set");
			for (String string : ckSigs) {
				System.out.println("\nSignature: " + string);
				correctSet.add(string.trim());
			}
			String header = "module " + name + Constants.LINE_SEPARATOR;
	
			String sigs = "one sig ";
			String separador = "";
			
			System.out.println("\nCorrect Ser Filled:");
			for (String string : correctSet) {
				string = string.trim();
				System.out.println("\nCorrect Item: " + string);
				if (string != null && !string.equals("")) {
					sigs += separador + string;
					separador = ", ";
				}
			}
			sigs += " in Bool{}" + Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR;
	
			String assertText = "assert WT {semantica" + indicator + "[] => semanticaCK[]}";
			assertText += Constants.LINE_SEPARATOR;
			assertText += "check WT for 2";
	
			String fileName = Constants.ALLOY_PATH + name + Constants.ALLOY_EXTENSION;
			String content = header + alloy + sigs + fMSemantics + Constants.LINE_SEPARATOR + assertText;
			System.out.println("\nCreate Alloy CK File:\nFile Name: " + fileName + "\nContent: " + content);
			/* Create Alloy Ck File: FileName and Content*/
			FileManager.getInstance().createFile(fileName, content);
		}
		
		/**
		 * This method checks the Safe Composition of the SPL.	 <br></br>
		 * @param string
		 * @param features
		 * @param name
		 * @return returns a SafeCompositionResult
		 * @see SafeCompositionResult
		 */
		private SafeCompositionResult checkSafeCompositionOfLine(String string, HashSet<String> features, String name) {
			System.out.println("\n\n\t\tThe beginning of the safe composition test to the "+ name + " SPL\n");
			Iterator<String> i = features.iterator();
			System.out.println("\nFeatures: < " + features.size() + " >");
			String featuresList = "";
			while(i.hasNext()){
				String s = (String) i.next();
				featuresList = featuresList + " [ " + s + " ] ";
			}
			System.out.println(featuresList);
			SafeCompositionResult checkCKSource = null;
			try {
				checkCKSource = SafeCompositionVerifier.checkCK(Constants.ALLOY_PATH, string, Constants.ALLOY_EXTENSION, string + Constants.ALLOY_EXTENSION, features, name);
			} catch (Err e) {
				System.out.println("\nAn Error Occurred when trying to do Safe Composition Test.\n\n" + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("\n\t\tEnd of Safe Composition test to the + "+ name + " SPL\n");
			return checkCKSource;
		}
		
		/**
		 * This method checks if the SPL is well formed.<br></br>
		 * @param sourceLine SOURCE product line.  <br></br>
		 * @param targetLine TARGET product line.  <br></br>
		 * @return if the SPL is well formed.  <br></br>
		 */
		public boolean isWF(ProductLine sourceLine, ProductLine targetLine) {
			
			System.out.println("\nBuild the SOURCE Feature Model Alloy file:");
			this.buildFMAlloyFile("source", Constants.ALLOY_PATH + Constants.SOURCE_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, sourceLine);
			
			System.out.println("\nBuild the TARGET Feature Model Alloy file:");
			this.buildFMAlloyFile("target", Constants.ALLOY_PATH + Constants.TARGET_FM_ALLOY_NAME + Constants.ALLOY_EXTENSION, targetLine);
	
			System.out.println("\nBuild the SOURCE Configuration Knowledge Alloy file:");
			this.buildAlloyCKFile(Constants.SOURCE_CK_ALLOY_NAME, this.featureModelSourceSemantics, "source", sourceLine);
			
			System.out.println("\nBuild the TARGET Configuration Knowledge Alloy file:");
			this.buildAlloyCKFile(Constants.TARGET_CK_ALLOY_NAME, this.featureModelTargetSemantics, "target", targetLine);
	
		    /* Well Formedness to the <Source> SPL */
			SafeCompositionResult sourceComposition = checkSafeCompositionOfLine(Constants.SOURCE_CK_ALLOY_NAME, sourceLine.getFeatures(), "source");
			SPLOutcomes.getInstance().setSourceIsWellFormed(!sourceComposition.getAnalysisResult());
			System.out.println("Well Formedness to the <Source> SPL: " + !sourceComposition.getAnalysisResult());
	
			/*Well Formedness to the <Target> SPL*/
			SafeCompositionResult targetComposition = checkSafeCompositionOfLine(Constants.TARGET_CK_ALLOY_NAME, targetLine.getFeatures(), "target");
			SPLOutcomes.getInstance().setTargetIsWellFormed(!targetComposition.getAnalysisResult());
			System.out.println("Well Formedness to the <Target> SPL.: " + !targetComposition.getAnalysisResult());
	
			return !sourceComposition.getAnalysisResult() && !targetComposition.getAnalysisResult();
			/* End of th Well Formedness Test */ 
		}
	
}
