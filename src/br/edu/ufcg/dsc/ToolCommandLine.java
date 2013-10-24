package br.edu.ufcg.dsc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.HTMLReader;

import org.eclipse.jdt.core.JavaModelException;
import br.edu.ufcg.saferefactor.core.*;

import safeEvolution.alloy.products.AlloyProductGenerator;
import safeEvolution.am.verifier.AssetMappingAnalyzer;
import safeEvolution.approaches.AllProductPairs;
import safeEvolution.approaches.AllProducts;
import safeEvolution.approaches.BackwardImpactedClasses;
import safeEvolution.approaches.ForwardImpactedClasses;
import safeEvolution.approaches.ImpactedProducts;
import safeEvolution.fileProperties.FilePropertiesObject;
import safeEvolution.inputFiles.xml.HtmlReader;
import safeEvolution.productMatcher.ProductMatching;
import safeEvolution.productsCleaner.ProductsCleaner;
import safeEvolution.wellFormedness.WellFormedness;
import br.edu.ufcg.dsc.builders.MobileMediaBuilder;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.builders.TargetBuilder;
import br.edu.ufcg.dsc.ck.ConfigurationItem;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import edu.mit.csail.sdg.alloy4.Err;

public class ToolCommandLine {

	private ProductsCleaner productsCleaner;
	
	private AlloyProductGenerator alloyProductGenerator;
	
	private WellFormedness wellFormedness;
	
	private ProductBuilder productBuilder;
	
	private AssetMappingAnalyzer amAnalyzer;
	
	private boolean alreadyVerified;
	
	private HashSet<String> changedFeatureNames;
	
	private ProductLine sourceSPL;
	
	private ProductLine targetSPL;
	
	private boolean wf;
	
	private HashSet<String> changedFeatures;
	
	private boolean areAllProductsMatched;
	
	private boolean isAssetMappingsEqual;
	
	private Properties properties;
	
	private SpreadSheetExecution sheetExecution;
	
	

	public ToolCommandLine() {
		this.productsCleaner = new ProductsCleaner();
		this.amAnalyzer = new AssetMappingAnalyzer();
	}

	public ToolCommandLine(Lines line) {
		this();
		this.properties = new Properties();
		if (line.equals(Lines.MOBILE_MEDIA)) {
			this.productBuilder = new MobileMediaBuilder();
		} else if (line.equals(Lines.TARGET)  || line.equals(Lines.DEFAULT)) {
			this.productBuilder = TargetBuilder.getInstance();
		}
		this.alloyProductGenerator = new AlloyProductGenerator(wellFormedness,this.productBuilder);
	}
	
	private void setup(ProductLine souceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
		/* Removes all of the mappings from this map. The map will be empty after this call returns. */
		XMLReader.getInstance().reset();
		/* Cleans the generated products folder. */
		this.productsCleaner.cleanProductsFolder();
		souceLine.setup();
		targetLine.setup();
	}

	private HashSet<String> getChangedFeatureNames(ProductLine targetLine) {

		if(this.alreadyVerified)
			return this.changedFeatureNames;
		
		this.changedFeatureNames = new HashSet<String>();
		if (this.amAnalyzer.getChangedAssetsList() == null) {
			return null;
		}

		HashSet<String> changedAssetNames = new HashSet<String>();

		for (String asset : this.amAnalyzer.getChangedAssetsList()) {
			String correspondingAssetName = getCorrespondingAssetName(targetLine.getAssetMapping(), asset);
			if (correspondingAssetName != null) {
				changedAssetNames.add(correspondingAssetName);
			}
		}

		ConfigurationKnowledge configutarionKnowledge = targetLine.getCk();

		Set<ConfigurationItem> ckItems = configutarionKnowledge.getCkItems();

		for (ConfigurationItem configurationItem : ckItems) {
			IFeatureExpression featExp = configurationItem.getFeatExp();
			Set<Task> tasks = configurationItem.getTasks();

			for (Task task : tasks) {
				Set<String> provided = task.getProvided().keySet();
				for (String string : changedAssetNames) {
					if (provided.contains(string)) {
						this.changedFeatureNames.add(featExp.getCode());
					}
				}
			}
		}
		this.alreadyVerified = true;
		return this.changedFeatureNames;
	}

	private String getCorrespondingAssetName(HashMap<String, String> assetMapping, String asset) {
		String result = null;

		asset = asset.replaceAll(Pattern.quote("\\"), "/");

		for (String assetName : assetMapping.keySet()) {
			String assetPath = assetMapping.get(assetName);

			if (assetPath.contains(asset.replaceAll(Pattern.quote("\\"), "/"))) {
				result = assetName;
			}
		}

		return result;
	}
	
	public void commonInfoBetweenApproaches (FilePropertiesObject input)throws Err, IOException, AssetNotFoundException, DirectoryException{
		String fachadaSource = null;
		String fachadaTarget = null; 
		
		String ckSource = input.getArtifactsSourceDir() + "ConfigurationKnowledge.xml";
		String ckTarget = input.getArtifactsTargetDir() + "ConfigurationKnowledge.xml";
		
		String fmSource = input.getArtifactsSourceDir() + "FeatureModel.xml";
		String fmTarget = input.getArtifactsTargetDir() + "FeatureModel.xml";
		
		String amSource = input.getArtifactsSourceDir() + "ComponentModel.txt";
		String amTarget = input.getArtifactsTargetDir() + "ComponentModel.txt";
		
		this.sourceSPL = new ProductLine(input.getSourceLineDirectory(), ckSource, fmSource, amSource, input.isAspectsInSourceSPL(), fachadaSource, input.getCkFormatSourceSPL(),input.getAmFormatSourceSPL());
		this.targetSPL = new ProductLine(input.getTargetLineDirectory(), ckTarget, fmTarget, amTarget, input.isAspectsInTargetSPL(), fachadaTarget, input.getCkFormatTargetSPL(), input.getAmFormatTargetSPL());
		this.sourceSPL.setLibPath(input.getSourceLineLibDirectory());
		this.targetSPL.setLibPath(input.getTargetLineLibDirectory());
		
	 	/* It cleans the generated products folder. */
		this.setup(sourceSPL, targetSPL);

		/* It Calls alloy to build source and target products and put it in cache. */
		this.alloyProductGenerator.generateProductsFromAlloyFile(this.sourceSPL, this.targetSPL);

		/* Reset results variables .*/
		SPLOutcomes sOutcomes = SPLOutcomes.getInstance();
		sOutcomes.getMeasures().reset();
		sOutcomes.getMeasures().setApproach(input.getApproach());
		sOutcomes.getMeasures().getTempoTotal().startContinue();

		this.wf = WellFormedness.getInstance().isWF(this.sourceSPL, this.targetSPL); 
		
		long initTime = System.currentTimeMillis();
		//this.areAllProductsMatched = ProductMatching.getInstance(productBuilder).areAllProductsMatched(this.sourceSPL, this.targetSPL);
		this.areAllProductsMatched =  true;
		System.out.println("areAllProductsMatched: " + areAllProductsMatched);
		long stTime = System.currentTimeMillis();
		long finishTime = stTime - initTime;
		System.out.println("\nTotal Time Spent to verify Products Matching: " + finishTime/60000 + " minutes");
		
		try {
			long startTime = System.currentTimeMillis();
			this.isAssetMappingsEqual = amAnalyzer.isSameAssets(this.sourceSPL, this.targetSPL);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			long diffTime =  elapsedTime/1000;
			System.out.println("\nTotal Time Spent to verify Modified Assets: " + diffTime + "seconds");
			System.out.println("\n AM Equal: " + isAssetMappingsEqual);
			sOutcomes.setAssetMappingsEqual(isAssetMappingsEqual);
			sOutcomes.setDiffTime(diffTime);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		this.changedFeatures = getChangedFeatureNames(this.targetSPL);
		
		long startTime = System.currentTimeMillis();
		this.amAnalyzer.findExtendedImpactedClasses(new File(input.getSourceLineDirectory()+"src"));
		
		verifyBranchNumber(input);
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		long findEicTime = elapsedTime/1000; // seconds
		sOutcomes.setFindEicTime(findEicTime);
		
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy   HH:mm:ss").format(Calendar.getInstance().getTime());
 		BufferedWriter logFile = LogFile.getInstance().getLog();
 		logFile.append("------------------------------------------------------------------------------" + "\n");
 		logFile.newLine();
 		logFile.append(timeStamp + "\n");
		logFile.newLine();
		logFile.append("Pair ID: " + input.getEvolutionDescription() + "\n");
		logFile.append("\nImpacted Classes:\n\n" + amAnalyzer.getModifiedClassesList());
		logFile.newLine();
		logFile.append("\nExtended Impacted Classes:\n\n" + amAnalyzer.getExtendedImpactedClasses());
		logFile.newLine();
		logFile.flush();
	}

	private void verifyBranchNumber(FilePropertiesObject input) {
		Collection<String> eic = new HashSet<String>();
		Collection<String> ic = new HashSet<String>();
		if (input.getEvolutionDescription().equals("branch1.0")){
			ic.add("TaRGeT HTML Output Plug-in.src.br.ufpe.cin.target.htmloutput.Activator.java");
			eic.add("TaRGeT HTML Output Plug-in.src.br.ufpe.cin.target.htmloutput.Activator.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch13.0")){
			ic.add("TaRGeT Use Case Editor.src.br.ufpe.cin.target.uceditor.editor.UseCaseEditor.java");
			eic.add("TaRGeT Use Case Editor.src.br.ufpe.cin.target.uceditor.editor.UseCaseEditorInput.java");
			eic.add("TaRGeT Use Case Editor.src.br.ufpe.cin.target.uceditor.editor.UseCaseEditorPage.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch14.0")){
			ic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.exceptions.DuplicatedTermInLexiconException.java");
			eic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.exceptions.UseCaseDocumentXMLException.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch6.0")){
			ic.add("TaRGeT Consistency Manager.src.java.br.ufpe.cin.target.cm.wizard.ConsistencyManagementWizard.java");
			eic.add("TaRGeT Consistency Manager.src.java.br.ufpe.cin.target.cm.wizard.ConsistencyManagementWizard.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch15.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.AdjectiveTerm.java");
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.AdverbTerm.java");
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.VerbTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.AdjectiveTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.AdverbTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.VerbTerm.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch16.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.DeterminerTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.DeterminerTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.LexicalEntry.java");			
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if (input.getEvolutionDescription().equals("branch17.0")){
			eic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.preferences.PreferencesDialog.java");
			eic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.editors.OnTheFlyUtil.java");
			ic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.editors.OnTheFlyEditorInput.java");
			ic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.editors.OnTheFlyUtil.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch18.0")){
			ic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.lts.LTS.java");
			eic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.lts.UserViewLTSGenerator.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch19.0")){
			ic.add("TaRGeT Consistency Manager.src.java.com.motorola.btc.research.target.cm.tcsimilarity.logic.StepSimilarity.java");
			eic.add("TaRGeT Consistency Manager.src.java.com.motorola.btc.research.target.cm.tcsimilarity.metrics.Metrics.java");
			eic.add("TaRGeT Consistency Manager.src.java.com.motorola.btc.research.target.cm.tcsimilarity.metrics.Metrics2.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch20.0")){
			ic.add("TaRGeT XLS Input Plug-in.src.com.motorola.btc.research.target.xlsinput.extractor.PhoneDocumentExtractor.java");
			eic.add("TaRGeT XLS Input Plug-in.src.com.motorola.btc.research.target.xlsinput.controller.XLSDocumentExtensionImplementation.java");
			eic.add("TaRGeTTest.java.com.motorola.btc.research.target.common.UnitTestUtil.java");
			eic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch22.0")){
			ic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.ucdoc.Feature.java");
			eic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch23.0")){
			ic.add("TaRGeT XLS Input Plug-in.src.com.motorola.btc.research.target.xlsinput.extractor.PhoneDocumentExtractor.java");
			eic.add("TaRGeT XLS Input Plug-in.src.com.motorola.btc.research.target.xlsinput.controller.XLSDocumentExtensionImplementation.java");
			eic.add("TaRGeTTest.java.com.motorola.btc.research.target.common.UnitTestUtil.java");
			eic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch24.0")){
			ic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLProperties.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLPluginController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch21.0")){
			ic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLProperties.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLPluginController.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.views.CNLView.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch25.0")){
			ic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLPluginController.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.views.CNLView.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch26.0")){
			ic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			eic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch27.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.util.UtilNLP.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.dialogs.AddLexicalTermDialog.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch28.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.vocabulary.terms.VerbTerm.java");
			eic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.util.UtilNLP.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch29.0")){
			ic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.ucdoc.Feature.java");
			eic.add("TaRGeT XLS Input Plug-in.src.com.motorola.btc.research.target.xlsinput.extractor.PhoneDocumentExtractor.java");
			eic.add("TaRGeT Common.src.java.com.motorola.btc.research.target.common.ucdoc.xml.UseCaseDocumentXMLParser.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch30.0")){
			ic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.extractor.TextualTestCaseFactory.java");
			eic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.controller.TestCaseGeneratorController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch31.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.util.UtilNLP.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.dialogs.AddLexicalTermDialog.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch32.0")){
			ic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.extractor.TextualTestCaseFactory.java");
			eic.add("TaRGeT TC Generation GUI.src.java.com.motorola.btc.research.target.tcg.controller.TestCaseGeneratorController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch33.0")){
			ic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.extensions.input.InputDocumentExtensionFactory.java");
			eic.add("TaRGeT Project Manager.src.java.com.motorola.btc.research.target.pm.controller.ProjectManagerController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch34.0")){
			ic.add("TaRGeT Consistency Manager.src.java.com.motorola.btc.research.target.cm.tcsimilarity.metrics.Metrics.java");
			eic.add("TaRGeT Consistency Manager.src.java.com.motorola.btc.research.target.cm.controller.ConsistencyManagementController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("branch35.0")){
			ic.add("CNL Framework.src.java.com.motorola.btc.research.cnlframework.preprocessor.TextPreprocessor.java");
			eic.add("TaRGeT CNL Plugin.src.java.com.motorola.btc.research.target.cnl.controller.CNLPluginController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("MobileMedia1.0")){
			ic.add("lancs.mobilemedia.core.ui.datamodel.ImageData.java");
			eic.add("lancs.mobilemedia.core.util.ImageUtil.java");
			eic.add("lancs.mobilemedia.core.ui.datamodel.ImageAccessor.java");
			eic.add("lancs.mobilemedia.core.ui.datamodel.AlbumData.java");
			eic.add("lancs.mobilemedia.core.ui.controller.BaseController.java"); // RETIRAR BaseController
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("MobileMedia2.0")){
			ic.add("lancs.mobilemedia.core.util.ImageUtil.java");
			ic.add("lancs.mobilemedia.core.ui.datamodel.ImageData.java");
			eic.add("lancs.mobilemedia.core.ui.datamodel.ImageAccessor.java");
			eic.add("lancs.mobilemedia.core.ui.datamodel.AlbumData.java");
			eic.add("lancs.mobilemedia.core.ui.controller.PhotoController.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}else if(input.getEvolutionDescription().equals("MobileMedia3.0")){
			ic.add("lancs.mobilemedia.core.ui.datamodel.ImageData.java");
			eic.add("lancs.mobilemedia.core.util.ImageUtil.java");
			this.amAnalyzer.setModifiedClassesList(ic);
			this.amAnalyzer.setExtendedImpactedClasses(eic);
		}
		
	}
	
	public void runApproach(FilePropertiesObject input) throws AssetNotFoundException, IOException, DirectoryException{
		BufferedWriter logFile = LogFile.getInstance().getLog();
 		logFile.newLine();
 		logFile.append("Approach: " + input.getApproach() + "<" + input.getGenerateTestsWith() + ">\n");
		logFile.flush();
		SPLOutcomes sOutcomes = SPLOutcomes.getInstance();
		long approachTime;
		boolean isRefinement = false;
		long elapsedTime = 0;
		if(input.getApproach().equals(Approach.APP)){
			System.out.println("\nALL PRODUCT PAIRS\n");
			AllProductPairs app = new AllProductPairs(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = app.evaluate(sourceSPL, targetSPL, input, wf)));
		}else if(input.getApproach().equals(Approach.AP)){
			System.out.println("\nALL PRODUCTS\n");
			AllProducts ap = new AllProducts(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = ap.evaluate(sourceSPL, targetSPL, input, wf, areAllProductsMatched)));
		}else if(input.getApproach().equals(Approach.IP)){
			System.out.println("\nIMPACTED PRODUCTS\n");
			ImpactedProducts ip = new ImpactedProducts(this.productBuilder, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ip.evaluate(sourceSPL, targetSPL, input, wf, areAllProductsMatched)));
		}else if(input.getApproach().equals(Approach.IC)){
			System.out.println("\nIMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			ForwardImpactedClasses ic = new ForwardImpactedClasses(productBuilder, input, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched)));
			long stopTime = System.currentTimeMillis();
		    elapsedTime = stopTime - startTime;
		    approachTime = (elapsedTime/1000) + sOutcomes.getDiffTime(); // seconds.
		    sOutcomes.setApproachTime(approachTime);
		    System.out.println("\n\n TIME SPENT IN THIS IC APPROACH: " + approachTime + " seconds");
		}else if(input.getApproach().equals(Approach.EIC)){
			System.out.println("\nEXTENDED IMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			BackwardImpactedClasses eic = new BackwardImpactedClasses(productBuilder, input, amAnalyzer.getExtendedImpactedClasses());
			isRefinement = eic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched, amAnalyzer.getModifiedClassesList() );
			long stopTime = System.currentTimeMillis();
		    elapsedTime = stopTime - startTime;
		    approachTime = (elapsedTime/1000) + sOutcomes.getDiffTime() + sOutcomes.getFindEicTime(); // seconds.
		    sOutcomes.setApproachTime(approachTime);
		    System.out.println("\n\n TIME SPENT IN THIS EIC APPROACH: " + approachTime + " seconds");
		}
		/*Report Variables: Pause total time to check the SPL.*/
		sOutcomes.getMeasures().setApproach(input.getApproach());
		sOutcomes.setWF(wf);
		sOutcomes.setFmAndCKRefinement(areAllProductsMatched);
		sOutcomes.setRefinement(wf && isRefinement);
		sOutcomes.setCompObservableBehavior(isRefinement);
	}
	
	public void persitResultsInPropertyFile(FilePropertiesObject input) throws IOException {
		SPLOutcomes sOutcomes = SPLOutcomes.getInstance();
		System.out.println("\nResult: " + sOutcomes.toString());
		String approachTool = input.getApproach()+ "-" + input.getGenerateTestsWith(); 
		String refinementOrNot = sOutcomes.isRefinement() ? "Refinement" : "Non-Refinement";

		String averageCoverage = "-";
		if(input.getGenerateTestsWith().equals("evosuite")){
			String htmlFile = Constants.PRODUCTS_DIR + "/Product0/source/src/evosuite-report/report-generation.html";
			averageCoverage =  HtmlReader.getAverageOfCoverage(htmlFile);	
		}
		
		properties.setProperty(approachTool, refinementOrNot +","+ sOutcomes.getApproachTime()+ "," + averageCoverage);
		properties.setProperty("pairId", input.getEvolutionDescription());
	}
	
	public void writeResultsInSpreadSheet(){
		this.sheetExecution = new SpreadSheetExecution();
		this.sheetExecution.storePropertiesInSpreadSheet(this.properties);
	}

	public boolean verifyLine(FilePropertiesObject in) throws Err, IOException, AssetNotFoundException, DirectoryException {

		String fachadaSource = null;
		String fachadaTarget = null; 
		
		String ckSource = in.getArtifactsSourceDir() + "ConfigurationKnowledge.xml";
		String ckTarget = in.getArtifactsTargetDir() + "ConfigurationKnowledge.xml";
		
		String fmSource = in.getArtifactsSourceDir() + "FeatureModel.xml";
		String fmTarget = in.getArtifactsTargetDir() + "FeatureModel.xml";
		
		String amSource = in.getArtifactsSourceDir() + "ComponentModel.txt";
		String amTarget = in.getArtifactsTargetDir() + "ComponentModel.txt";
		
		ProductLine sourceSPL = new ProductLine(in.getSourceLineDirectory(), ckSource, fmSource, amSource, in.isAspectsInSourceSPL(), fachadaSource, in.getCkFormatSourceSPL(),in.getAmFormatSourceSPL());
		ProductLine targetSPL = new ProductLine(in.getTargetLineDirectory(), ckTarget, fmTarget, amTarget, in.isAspectsInTargetSPL(), fachadaTarget, in.getCkFormatTargetSPL(), in.getAmFormatTargetSPL());
		sourceSPL.setLibPath(in.getSourceLineLibDirectory());
		targetSPL.setLibPath(in.getTargetLineLibDirectory());
		
	 	/* It cleans the generated products folder. */
		this.setup(sourceSPL, targetSPL);

		/* It Calls alloy to build source and target products and put it in cache. */
		this.alloyProductGenerator.generateProductsFromAlloyFile(sourceSPL, targetSPL);

		/* Reset results variables .*/
		SPLOutcomes sOutcomes = SPLOutcomes.getInstance();
		sOutcomes.getMeasures().reset();
		sOutcomes.getMeasures().setApproach(in.getApproach());
		sOutcomes.getMeasures().getTempoTotal().startContinue();

		boolean wf = WellFormedness.getInstance().isWF(sourceSPL, targetSPL); 
		
		HashSet<String> changedFeatures = getChangedFeatureNames(targetSPL);
		
		boolean areAllProductsMatched = ProductMatching.getInstance(productBuilder).areAllProductsMatched(sourceSPL, targetSPL);
		System.out.println("areAllProductsMatched: " + areAllProductsMatched);
		
		boolean isAssetMappingsEqual;
		try {
			isAssetMappingsEqual = amAnalyzer.isSameAssets(sourceSPL, targetSPL);
			System.out.println("\n AM Equal: " + isAssetMappingsEqual);
			sOutcomes.setAssetMappingsEqual(isAssetMappingsEqual);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		boolean isRefinement = false;
		if(in.getApproach().equals(Approach.APP)){
			System.out.println("\nALL PRODUCT PAIRS\n");
			AllProductPairs app = new AllProductPairs(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = app.evaluate(sourceSPL, targetSPL, in, wf)));
		}else if(in.getApproach().equals(Approach.AP)){
			System.out.println("\nALL PRODUCTS\n");
			AllProducts ap = new AllProducts(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = ap.evaluate(sourceSPL, targetSPL, in, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.IP)){
			System.out.println("\nIMPACTED PRODUCTS\n");
			ImpactedProducts ip = new ImpactedProducts(this.productBuilder, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ip.evaluate(sourceSPL, targetSPL, in, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.IC)){
			System.out.println("\nIMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			ForwardImpactedClasses ic = new ForwardImpactedClasses(productBuilder, in, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched)));
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("\n\n TIME SPENT IN THIS IC APPROACH: " + elapsedTime/1000 + " milliseconds");
		}else if(in.getApproach().equals(Approach.EIC)){
			System.out.println("\nEXTENDED IMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			BackwardImpactedClasses eic = new BackwardImpactedClasses(productBuilder, in, amAnalyzer.getExtendedImpactedClasses());
			isRefinement = eic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched, amAnalyzer.getModifiedClassesList());
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("\n\n TIME SPENT IN THIS EIC APPROACH: " + elapsedTime/1000 + " milliseconds");
		}
		
		/*Report Variables: Pause total time to check the SPL.*/
		sOutcomes.setWF(wf);
		
		sOutcomes.setFmAndCKRefinement(areAllProductsMatched);
		sOutcomes.setRefinement(wf && isRefinement);
		sOutcomes.setCompObservableBehavior(isRefinement);
		sOutcomes.getMeasures().getTempoTotal().pause();
		sOutcomes.getMeasures().print();

		return isRefinement;
	}
	
	

	public void setAmAnalyzer(AssetMappingAnalyzer amAnalyzer) {
		this.amAnalyzer = amAnalyzer;
	}
	public AssetMappingAnalyzer getAmAnalyzer() {
		return amAnalyzer;
	}
	public Properties getProperties(FilePropertiesObject input) {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	
}
